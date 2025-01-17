/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.cql.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.junit.Test;

import com.ibm.cohort.cql.library.CqlLibrary;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor;
import com.ibm.cohort.cql.library.CqlLibraryDescriptor.Format;
import com.ibm.cohort.cql.library.CqlLibraryProvider;
import com.ibm.cohort.cql.library.DirectoryBasedCqlLibraryProvider;

public class TranslatingCqlLibraryProviderTest {
    @Test
    public void testLoadWithTranslation() throws Exception {
        CqlToElmTranslator translator = new CqlToElmTranslator();
        try( Reader modelInfoXML = new FileReader("src/test/resources/modelinfo/mock-modelinfo-1.0.0.xml") ) {
            translator.registerModelInfo(modelInfoXML);
        }
        
        CqlLibraryProvider backingProvider = new DirectoryBasedCqlLibraryProvider(new File("src/test/resources/cql"));
        CqlLibraryProvider provider = new TranslatingCqlLibraryProvider(backingProvider, translator);
        
        assertEquals( backingProvider.listLibraries(), provider.listLibraries() );
        
        CqlLibraryDescriptor descriptor = new CqlLibraryDescriptor()
                .setLibraryId("CohortHelpers")
                .setVersion("1.0.0")
                .setFormat(Format.CQL);
        
        CqlLibrary library = provider.getLibrary(descriptor);
        assertEquals( Format.CQL, library.getDescriptor().getFormat() );
        assertTrue( library.getContent().startsWith("library") );
        
        descriptor.setFormat(Format.ELM);
        library = provider.getLibrary(descriptor);
        assertEquals( Format.ELM, library.getDescriptor().getFormat() );
        assertTrue( library.getContent().startsWith("<?xml") );
    }
}
