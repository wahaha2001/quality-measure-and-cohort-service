/*
 * (C) Copyright IBM Corp. 2020, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.cohort.translator.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import com.ibm.cohort.annotations.Generated;
import com.ibm.cohort.file.LibraryFormat;

import org.cqframework.cql.cql2elm.CqlTranslator.Options;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.elm.execution.Library;

/**
 * Common functionality for use when implementing CQL translator
 * wrapper implementations.
 */
@Generated
public abstract class BaseCqlTranslationProvider implements CqlTranslationProvider {

	public static final LibraryFormat DEFAULT_TARGET_FORMAT = LibraryFormat.XML;
	
	public List<Options> getDefaultOptions() {
		List<Options> defaults = new ArrayList<>(CqlTranslatorOptions.defaultOptions().getOptions());
		//defaults.add( CqlTranslator.Options.EnableDateRangeOptimization );
		return defaults;
	}

	@Override
	public Library translate(InputStream cql) throws Exception {
		return translate( cql, getDefaultOptions() );
	}

	@Override
	public Library translate(InputStream cql, List<Options> options) throws Exception {
		return translate(cql, options, DEFAULT_TARGET_FORMAT);
	}

	@Override
	public Library translate(String cql) throws Exception {
		return translate( cql, getDefaultOptions() );
	}

	public Library translate(String cql, List<Options> options) throws Exception {
		return translate(cql, options, DEFAULT_TARGET_FORMAT);
	}
}