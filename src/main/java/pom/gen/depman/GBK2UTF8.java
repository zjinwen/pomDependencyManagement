package pom.gen.depman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class GBK2UTF8 {

	public static void main(String[] args) throws Exception {
		
		
	  String dir="D:\\del\\file-transfer";
	  if(args.length>0){
		  dir=args[0];
	  }
	  
	  File file=new File(dir);
	  doFile(file);
	}

	private static void doFile(File file) throws Exception {
		for(File f:file.listFiles()){
			  if(f.isFile()&&f.getName().endsWith(".java")){
				  gbk2Utf8(f);
			  }else if(f.isDirectory()){
				  doFile(f);
			  }
		  }
	}

	private static void gbk2Utf8(File f) throws Exception {
		 BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f),"GBK"));
		 File newFile=new File( f.getAbsolutePath()+"u");
		 PrintWriter writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream(newFile),"UTF-8"));
		 try{
		 String line=null;
		 while((line=reader.readLine())!=null){
			 writer.println(line);
		 }
		 reader.close();
		 writer.close();
		 
		 f.delete();
		 newFile.renameTo(f);
		 newFile.delete();
		 }finally{
			 if(reader!=null){
				 reader.close();
			 }
			 if(writer!=null){
				 writer.close();
			 }
			 
		 }
		 
	}

}
