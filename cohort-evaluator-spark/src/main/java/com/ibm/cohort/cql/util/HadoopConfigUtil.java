package com.ibm.cohort.cql.util;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkContext;
import org.apache.spark.deploy.SparkHadoopUtil;

public class HadoopConfigUtil {
	public static FileSystem getFileSystemForPath(Path path) throws IOException {
		return path.getFileSystem(SparkHadoopUtil.get().newConfiguration(SparkContext.getOrCreate().conf()));
	}
}
