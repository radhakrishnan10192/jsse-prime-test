package com.paypal.infra.ssl.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MockDataHelper {

    private static Logger LOG = LoggerFactory.getLogger(MockDataHelper.class);

    private static Properties properties = null;

    public List<String> getDelimitedValuesAsListOfString(String property, String delimeter){

        LOG.debug("Executing the method getDelimitedValuesAsList()..");
        //Getting the singleton instance of PropertiesFileReader object.
        Object data = getProperty(property);
        if(data == null){

            return new ArrayList<>();
        }
        return getInArrayList(String.valueOf(data), delimeter);
    }

    public String getString(String property) {

        LOG.debug("Executing the method getString()..");
        //Getting the singleton instance of PropertiesFileReader object.
        Object data = getProperty(property);
        if(data == null){

            return new String();
        }
        return String.valueOf(data);
    }

    public Integer getIntValue(String property) {

        LOG.debug("Executing the method getIntValue()..");
        try{
            //Getting the singleton instance of PropertiesFileReader object.
            return Integer.valueOf(String.valueOf(getProperty(property)));
        }catch (NumberFormatException e){
            LOG.error("The data in the property file is not of type integer");
            throw e;
        }
    }

    public Object getPropertyAsObject(String property, Class<?> cls) throws JsonProcessingException {

        LOG.debug("Executing the method getPropertyAsObject()..");
        ObjectMapper mapper = new ObjectMapper();
        try {
            //Getting the singleton instance of PropertiesFileReader object.
            return mapper.readValue(getProperty(property).toString(), cls);
        } catch (JsonProcessingException e) {
            LOG.error("Error mapping the values to the class + " + cls.getName());
            throw e;
        }
    }

    public List<Object> getPropertiesAsListOfObjects(String property) throws JsonProcessingException {

        LOG.debug("Executing the method getPropertiesAsListOfObjects()..");
        ObjectMapper mapper = new ObjectMapper();
        List<Object> objects = new ArrayList<>();
        Object data = getProperty(property);

        if(data == null){

            return objects;
        }
        try {
            objects = mapper.readValue(data.toString(), new TypeReference<List<Object>>(){});
        } catch (JsonProcessingException e) {
            LOG.error("Error mapping the values to the class" + e.getMessage());
        }
        return objects;
    }

    private List<String> getInArrayList(String s, String delimeter) {

        return Arrays.asList(s.split(delimeter));
    }

    public Boolean getBoolean(String property){
        LOG.debug("Executing the method getBoolean");
        String result=getProperty(property).toString();
        if (result.equalsIgnoreCase("true") || result.equalsIgnoreCase("false")) {
            return Boolean.valueOf(result);
        } else {
            LOG.error("Error mapping the values to the boolean, returning false");
            //q: do we want program execution to stop if value is missing?
            return false;
        }
    }

    private static Object getProperty(final String propertyName) {
        return getMockProperties().get(propertyName);
    }

    public static Properties getMockProperties()  {
        if(properties != null) {
            return properties;
        }
        Properties properties = new Properties();
        try(InputStream inputStream = PropertiesFileReader
                .class.getClassLoader().getResourceAsStream("mockData.properties")) {
            properties.load(inputStream);
            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
