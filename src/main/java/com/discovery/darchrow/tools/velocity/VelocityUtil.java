package com.discovery.darchrow.tools.velocity;

import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

public final class VelocityUtil extends BaseVelocityUtil{
	  protected Context constructContext(Map<String, ?> contextKeyValues)
	  {
	    return new VelocityContext(contextKeyValues);
	  }
}
