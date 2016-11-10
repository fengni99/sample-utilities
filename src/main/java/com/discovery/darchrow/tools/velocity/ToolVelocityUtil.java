package com.discovery.darchrow.tools.velocity;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.config.ConfigurationUtils;
import org.apache.velocity.tools.config.FactoryConfiguration;

public class ToolVelocityUtil extends BaseVelocityUtil{
	protected Context constructContext(Map<String, ?> contextKeyValues)
	  {
	    FactoryConfiguration genericTools = ConfigurationUtils.getGenericTools();
	    
	    ToolManager toolManager = new ToolManager();
	    toolManager.configure(genericTools);
	    
	    @SuppressWarnings({ "unchecked", "rawtypes" })
		ToolContext toolContext = toolManager.createContext(new HashMap(contextKeyValues));
	    toolContext.putAll(contextKeyValues);
	    return toolContext;
	  }
}