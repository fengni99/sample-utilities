package com.mdl.sample.tools.velocity;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VMTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VMTest.class);
	
	@Test
	public void test(){
		Map<String, String> map =new HashMap<String, String>();
		map.put("user", "mdl");
		String html =new VelocityUtil().parseTemplateWithClasspathResourceLoader("test.vm", map);
		LOGGER.debug("result:{}", html);
	}
}
