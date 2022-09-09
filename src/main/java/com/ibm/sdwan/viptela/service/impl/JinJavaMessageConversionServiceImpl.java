package com.ibm.sdwan.viptela.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.Jinjava;
import com.ibm.sdwan.viptela.driver.SdwanResponseException;
import com.ibm.sdwan.viptela.model.ExecutionRequest;
import com.ibm.sdwan.viptela.service.MessageConversionException;
import com.ibm.sdwan.viptela.service.MessageConversionService;
import com.ibm.sdwan.viptela.service.MissingPropertyException;
import com.ibm.sdwan.viptela.service.TemplateException;
import com.ibm.sdwan.viptela.utils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("JinJavaMessageConversionServiceImpl")
public class JinJavaMessageConversionServiceImpl implements MessageConversionService {

    private static final Logger logger = LoggerFactory.getLogger(JinJavaMessageConversionServiceImpl.class);
    private static final String TEMPLATE_PATH = "templates/";

    @Override
    public String generateMessageFromRequest(String messageType, ExecutionRequest executionRequest) throws MessageConversionException {
        String fullTemplateName = messageType+ ".json";
        logger.debug("fullTemplateName  {} \n", fullTemplateName);
        final String template = getTemplateFromExecutionRequest(executionRequest, fullTemplateName);
        logger.debug("template file from ExecutionRequest {} \n", template);
        final Jinjava jinjava = new Jinjava();
        try {
            String returnVal = jinjava.render(template, createJinJavaContext(executionRequest.getProperties(), template));
            logger.info("Message conversion script successfully run, returnVal is\n{}", returnVal);
            return returnVal;
        } catch (IOException e) {
            throw new MessageConversionException("Exception caught executing a script", e);
        }
    }

    @Override
    public Map<String, Object> extractPropertiesFromMessage( String message) {
        return convertJsonToMap(message);
    }

    private Map<String, Object> convertJsonToMap(String jsonMessage) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        try {
            map = objectMapper.readValue(jsonMessage, Map.class);
            logger.debug("Map is " + map);
            logger.debug("Map Size is " + map.size());
        } catch (Exception e) {
            logger.error("Error while parsing json to map using jackson library");
            throw new SdwanResponseException("Error while parsing json to map using jackson library");
        }
        return map;
    }
    private String getTemplateFromExecutionRequest(final ExecutionRequest executionRequest, final String fullTemplateName) {

        String templateContents = FileUtils.getFileFromLifecycleScripts(executionRequest.getDriverFiles(), TEMPLATE_PATH + fullTemplateName);

        if (templateContents == null) {
            try (InputStream inputStream = JinJavaMessageConversionServiceImpl.class.getResourceAsStream("/" + TEMPLATE_PATH +  "/" + fullTemplateName)) {
                if (inputStream != null) {
                    templateContents = IOUtils.toString(inputStream, Charset.defaultCharset());
                }
            } catch (IOException e) {
                logger.error("Error while looking up default lifecycle script");
                throw new SdwanResponseException("Error while looking up default lifecycle script");
            }
        }
        if (templateContents != null) {
            return templateContents;
        } else {
            logger.error("Unable to find a template called "+fullTemplateName);
            throw new TemplateException(String.format("Unable to find a template called [%s]", fullTemplateName));
        }
    }


    private Map<String,Object> createJinJavaContext(Map<String, Object> resourceProperties, String templateContents) throws  IOException {
        Map<String, Object> context = new HashMap<>();
        List<String> list = findPropertyListFromTemplate(templateContents);
        logger.debug("list of properties in template {}", list);
        list.forEach(property ->{
            Object value = resourceProperties.get(property);
            logger.debug("property -->{} value ---> {}", property, value);
            if (value == null) {
                throw new MissingPropertyException("Missing value for resource property: " + property);
            }
            context.put(property, value);
        });
        return context;
    }

    private List<String> findPropertyListFromTemplate(String templateContents) {
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{(.*?)}}");
        Matcher match = pattern.matcher(templateContents);
        while(match.find()) {
            int start = match.start(0);
            int end = match.end(0);
            String extractedProperty = templateContents.substring(start + 2, end - 2);
            logger.debug("Extracted property {}", extractedProperty);
            list.add(extractedProperty.trim());
        }
        if(list.isEmpty()){
            logger.debug("No properties extracted from the template {}", templateContents);
        }
        return list;
    }
}
