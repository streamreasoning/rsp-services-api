package polimi.deib.csparql_rest_api.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Config {
	private static Config _instance = null;
	private static final Logger logger = LoggerFactory.getLogger(Config.class); 
	
	private Configuration config;
	
	private Config(){
		try {
			config = new PropertiesConfiguration("setup.properties");
		} catch (ConfigurationException e) {
			logger.error("Error while reading the configuration file", e);
		}
	}
	
	public String getServerVersion(){
		return config.getString("csparql_server.version");
	}
				
	public static Config getInstance(){
		if(_instance==null)
			_instance=new Config();
		return _instance;
	}
}
