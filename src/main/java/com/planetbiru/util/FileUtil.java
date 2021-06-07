package com.planetbiru.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.springframework.core.io.ClassPathResource;

public class FileUtil {
	private FileUtil()
	{
		
	}
	public static byte[] read(String fileName) throws FileNotFoundException
	{
		byte[] allBytes = null;
		try 
		(
			 InputStream inputStream = FileUtil.class.getResourceAsStream(fileName);
		) 
		{
			File resource = new ClassPathResource(fileName).getFile();		
			long fileSize = resource.length();
			allBytes = new byte[(int) fileSize];
			int length = inputStream.read(allBytes);
			if(length == 0)
			{
				allBytes = null;
			}
		 } 
		 catch (IOException ex) 
		 {
			 throw new FileNotFoundException(ex);
		 }
		 return allBytes;
	 }
	public static void write(String fileName, byte[] data) throws IOException
	{
		String dir = FileUtil.class.getResource("/").getFile();
        /**
         * String dir = FileUtil.class.getResource("/dir").getFile();
         */
        OutputStream os = new FileOutputStream(dir + fileName);
        final PrintStream printStream = new PrintStream(os);
        printStream.write(data);
        printStream.close();
	}
}