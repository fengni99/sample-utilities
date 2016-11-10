package com.discovery.darchrow.library.address;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.discovery.darchrow.tools.jsonlib.JsonUtil;

public class AddressUtilTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddressUtilTest.class);
	
	@Test
	public void test(){
		LOGGER.debug("address:{}", JsonUtil.format(AddressUtil.getAddressById(110000l)));
	}
}
