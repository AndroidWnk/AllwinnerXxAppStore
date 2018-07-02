package com.e_trans.xxappstore.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * 
 * 
 */

public class MD5Util {

	private final static String[] strDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

	public static String getFileMD5(String path) {
		String md5 = null;
		MessageDigest md5Inst = null;
		try {
			md5Inst = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		File file = new File(path);
		if (file.exists()) {
			if (file.length() / 1024 > 10) {
				return getLargeFileMD5(md5Inst,file);
			}
			DataInputStream inputStream  = null;
			try {
				inputStream = new DataInputStream(
						new FileInputStream(file));
				byte[] buffer = new byte[(int) file.length()];
				inputStream.readFully(buffer);

				byte[] buf = md5Inst.digest(buffer);
				md5 = byteToString(buf);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				if(inputStream!=null){
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return md5;
	}

	private static String getLargeFileMD5(MessageDigest md5Inst, File file) {
		String md5 = null;
		RandomAccessFile reader = null;
		ByteArrayOutputStream byteOutput = null;
		try {
			reader = new RandomAccessFile(file, "r");
			long file_size = 0;
			try {
				file_size = reader.length();
			} catch (IOException e1) {
			}
			int c;
			byteOutput = new ByteArrayOutputStream();
			file_size /=1024;
			for (c = 0; c < 10; c++) {
				byte buffer[] = new byte[1024];
				int len = 0;
				try {
					reader.seek(((file_size*1024)/10)*c);
					len = reader.read(buffer, 0, 1024);
				} catch (IOException e) {
					continue;
				}
				byteOutput.write(buffer,0,len);
			}
			
			byte[] buffer = new byte[10250];
			for(int i = 0; i < 10250; i++){
				buffer[i] = 0;
			}
			byte cp[] = byteOutput.toByteArray();
			System.arraycopy(cp, 0, buffer, 0, cp.length);
			
			byte[] buf = md5Inst.digest(buffer);
			md5 = byteToString(buf);
		} catch (FileNotFoundException e) {
			return md5;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			} finally {
				if (byteOutput != null) {
					try {
						byteOutput.close();
					} catch (IOException e) {
					}
				}
			}

		}
		return md5;
	}

	public static String getMD5(String str) {
		String md5 = null;
		MessageDigest md5Inst = null;
		if (str == null)
			return null;
		try {
			md5Inst = MessageDigest.getInstance("MD5");
			byte[] buf = md5Inst.digest(str.getBytes());
			md5 = byteToString(buf);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return md5;
	}

	private static String byteToArrayString(byte bByte) {
		int iRet = bByte;
		if (iRet < 0) {
			iRet += 256;
		}
		int iD1 = iRet / 16;
		int iD2 = iRet % 16;
		return strDigits[iD1] + strDigits[iD2];
	}

	private static String byteToString(byte[] bByte) {
		StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i < bByte.length; i++) {
			sBuffer.append(byteToArrayString(bByte[i]));
		}
		return sBuffer.toString();
	}

}
