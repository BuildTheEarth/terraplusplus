package net.buildtheearth.terraminusminus;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public class TerraMinusMinus {
	
    public static final Logger LOGGER = new SimpleLogger("[terra--]", Level.INFO, true, false, true, false, "[yyyy/MM/dd HH:mm:ss:SSS]", null, new PropertiesUtil("log4j2.simplelog.properties"), System.out);
    
}
