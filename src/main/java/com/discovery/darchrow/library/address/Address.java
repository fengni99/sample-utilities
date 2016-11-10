/**
 * Copyright (c) 2013 Baozun All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Baozun.
 * You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Baozun.
 *
 * BAOZUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. BAOZUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */
package com.discovery.darchrow.library.address;

/**
 * 物流地址
 * @author dongliang ma
 *
 */
public class Address {

	/** 地址id **/
	private Long id;
	
	/** 地址名字 **/
	private String name;
	
	/** 父一级地址的id **/
	private Long pId;
	
	/** 地址的拼写（汉语拼音或者英文地名） **/
	private String spelling;
	
	

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the pId
	 */
	public Long getpId() {
		return pId;
	}

	/**
	 * @param pId the pId to set
	 */
	public void setpId(Long pId) {
		this.pId = pId;
	}

	/**
	 * @return the spelling
	 */
	public String getSpelling() {
		return spelling;
	}

	/**
	 * @param spelling the spelling to set
	 */
	public void setSpelling(String spelling) {
		this.spelling = spelling;
	}
	
	
}
