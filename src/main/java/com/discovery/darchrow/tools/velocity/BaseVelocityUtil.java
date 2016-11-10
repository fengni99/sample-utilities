package com.discovery.darchrow.tools.velocity;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.discovery.darchrow.bean.ConvertUtil;
import com.discovery.darchrow.tools.jsonlib.JsonUtil;
import com.discovery.darchrow.util.ResourceBundleUtil;
import com.discovery.darchrow.util.Validator;

abstract class BaseVelocityUtil
{
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseVelocityUtil.class);
  private static String CONFIG_BASE_NAME = "config.velocity";
//  private static String LOGTAG = "feilongStringVelocity";
  private static String ENCODING = "UTF-8";
  protected static VelocityEngine velocityEngine = null;
  
  static
  {
	    Properties properties = ConvertUtil.toProperties(ConvertUtil.toMap(ResourceBundleUtil.getResourceBundle(CONFIG_BASE_NAME)));
	    
	    String message = "can't load [%s],this properties is use for init velocityEngine,Please make sure that the location of the file path";
	    Validate.notEmpty(properties, message, new Object[] { CONFIG_BASE_NAME });
	    if (LOGGER.isDebugEnabled()) {
	      LOGGER.debug("will init velocity use config:[{}], properties:{}", CONFIG_BASE_NAME, JsonUtil.format(ConvertUtil.toMap(properties)));
	    }
	    velocityEngine = new VelocityEngine();
	    velocityEngine.init(properties);
  }
  
  public String parseTemplateWithClasspathResourceLoader(String templateInClassPath, Map<String, ?> contextKeyValues)
  {
    Context context = constructContext(contextKeyValues);
    return parseTemplateWithClasspathResourceLoader(templateInClassPath, context);
  }
  
  public String parseTemplateWithClasspathResourceLoader(String templateInClassPath, Context context)
  {
    Validate.notBlank(templateInClassPath, "templateInClassPath can't be null/empty!", new Object[0]);
    Validate.notNull(context, "context can't be null!", new Object[0]);
    
    Writer writer = new StringWriter();
    
    Template template = velocityEngine.getTemplate(templateInClassPath, ENCODING);
    template.merge(context, writer);
    try
    {
      writer.flush();
    }
    catch (IOException e)
    {
      throw new VelocityException(e);
    }
    return writer.toString();
  }
  
  public String parseString(String vmContent, Map<String, ?> contextKeyValues)
  {
    Context context = constructContext(contextKeyValues);
    return parseString(vmContent, context);
  }
  
  public String parseString(String vmContent, Context context)
  {
    if (Validator.isNullOrEmpty(vmContent)) {
      return "";
    }
    Validate.notNull(context, "context can't be null!", new Object[0]);
    Writer writer = new StringWriter();
    
    velocityEngine.evaluate(context, writer, "", vmContent);
    return writer.toString();
  }
  
  protected abstract Context constructContext(Map<String, ?> paramMap);
}
