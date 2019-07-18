package com.ertu.news.utils;

import java.io.File;
import java.util.List;

/**
 * @author hxf
 */
public class TraverseFile {

	/**
	 *
	 * @param file	遍历的目录结构
	 * @param list	存放遍历结果的集合
	 */
	public static void traverseFileByRecurse(File file,List<File> list) {
		// 首先对入参文件进行判断，去除所有的异常情况
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles != null && childFiles.length > 0){
				for (File childFile : childFiles) {
					traverseFileByRecurse(childFile,list);
				}
			}
		}else {
			String fileName = file.getName();
			String standardConfig = "config.xml";
			if (standardConfig.equals(fileName)){
				list.add(file);
			}
		}
	}
	
}
