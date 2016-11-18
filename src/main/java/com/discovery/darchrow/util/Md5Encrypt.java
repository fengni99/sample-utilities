/*
 * Copyright (c) 2010 Jumbomart All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Jumbomart.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Jumbo.
 *
 * JUMBOMART MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. JUMBOMART SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */
package com.discovery.darchrow.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class Md5Encrypt{

	public static final String	MD5_ENCODEING	= "utf-8";

	private static ThreadLocal<MessageDigest> digesterContext = new ThreadLocal<MessageDigest>();

	/**
	 * 对字符串进行MD5加密
	 * 
	 * @param text
	 *            明文
	 * @return 密文
	 */
	public static String md5(String text, String encoding){
		if (text == null) return null;
		try {
			MessageDigest digester = digesterContext.get();
			if(digester == null){
				digester = MessageDigest.getInstance("MD5");
				digesterContext.set(digester);
			}
			
			digester.reset();
			return StringUtil.bytes2String(digester.digest(text.getBytes(encoding)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("System doesn't support MD5 algorithm.");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System doesn't support your  EncodingException.");
		}
	}
	
	/**
	 * get the md5 hash of a string
	 * for unionPay
	 * @param str
	 * @return
	 */
	public static String md5(String str) {
		if (str == null) return null;
		try {
			MessageDigest digester = digesterContext.get();
			if(digester == null){
				digester = MessageDigest.getInstance("MD5");
				digesterContext.set(digester);
			}
			
			digester.reset();
			return StringUtil.bytes2String(digester.digest(str.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException e) {
			return str;
		} catch (UnsupportedEncodingException e) {
			return str;
		}
	}
}
