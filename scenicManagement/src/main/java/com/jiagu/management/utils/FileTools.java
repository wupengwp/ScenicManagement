package com.jiagu.management.utils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.http.util.EncodingUtils;

public class FileTools {
	private static final boolean ON_OFF = true;
	private static final String SDCARD_DIR = "/mnt/sdcard/";
	
	//写数据到SD中的文件  
	public static void writeFileSdcardFile(String fileName,String content) throws IOException{   
	 try{   
		   String file = SDCARD_DIR+fileName;

	      //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(file, true);
            writer.write(content);
            writer.close();
	     }  
	  
	      catch(Exception e){   
	        e.printStackTrace();   
	       }   
	   }   
	  
	    
	//读SD中的文件  
	public static String readFileSdcardFile(String fileName) throws IOException{  
	  String res=""; 
	  
	  String file = SDCARD_DIR+fileName;
	  
	  try{   
	         FileInputStream fin = new FileInputStream(file);   
	  
	         int length = fin.available();   
	  
	         byte [] buffer = new byte[length];   
	         fin.read(buffer);       
	  
	         res = EncodingUtils.getString(buffer, "UTF-8");   
	  
	         fin.close();       
	        }   
	  
	        catch(Exception e){   
	         e.printStackTrace();   
	        }   
	        return res;   
	}  
	
	
	public static void writeLog(String fileName,String content){
		if (ON_OFF){
			try{
				Date dt = new Date();
				String log = dt.toLocaleString()+":"+content+"\n";
				writeFileSdcardFile(fileName, log);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	} 

}
