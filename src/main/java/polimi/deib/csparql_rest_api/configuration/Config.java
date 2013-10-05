/*******************************************************************************
 * Copyright 2013 DEIB - Politecnico di Milano
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
