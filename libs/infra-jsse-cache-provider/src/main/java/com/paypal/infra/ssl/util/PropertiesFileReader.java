package com.paypal.infra.ssl.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesFileReader {

    private Properties properties;
    private static PropertiesFileReader propertiesFileReader = null;

    private static Logger LOG = LoggerFactory.getLogger(PropertiesFileReader.class);

    private void PropertiesFileReader(){}

    /**
     * Loads properties from mockData.properties file
     * The preference for loading the properties is given to the application's mockData.properties.
     * If that is absent, as a fallback, it will be loaded by default from the library's mockData.properties
    **/
    private void loadProperties(String fileName) {

        InputStream input;
        if(!StringUtils.isEmpty(fileName)){

            input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);

            if(input != null){
                if(input instanceof BufferedInputStream){
                    LOG.debug("Loading properties from application resource folder.");
                }else{
                    LOG.debug("Loading default properties from the infra-jsse-cache-provider library.");
                }
                properties = new Properties();
                try {
                    properties.load(input);
                } catch (IOException e) {
                    LOG.error("Error occurred while loading properties " + e.getMessage());
                }
                LOG.debug("Properties file load successful with " + properties.size() +  " properties");
            }
        }
    }

    //Made this object static so it can be accessed from across the application.
    public static PropertiesFileReader getInstance()
    {
        if (propertiesFileReader == null) {
            propertiesFileReader = new PropertiesFileReader();
            propertiesFileReader.loadProperties("mockData.properties");
            return propertiesFileReader;
        }
        else {
            return propertiesFileReader;
        }
    }

    public Properties getProperties(){

        return properties;
    }

    protected Object get(String property){

        LOG.debug("Trying to get data for the property : " + property);
        Object val = properties.getOrDefault(property, null);
        if(val == null){
            LOG.error("Make sure the " + property +" property is available in mockData.properties file in your \n" +
                    "application resource folder, If you want to go with the default values, please delete the \n" +
                    "mockData.properties from your application resource folder");
        }
        return val;
    }

    public synchronized void setProperty(String key, String value) {

        LOG.debug("Trying to set data for the property : " + key);
        properties.setProperty(key, value);
    }
}
