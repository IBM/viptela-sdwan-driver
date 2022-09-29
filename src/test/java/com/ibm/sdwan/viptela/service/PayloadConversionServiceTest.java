 package com.ibm.sdwan.viptela.service;

 import static com.ibm.sdwan.viptela.utils.Constants.SMART_ACCOUNT_PASSWORD;
 import static com.ibm.sdwan.viptela.utils.Constants.SMART_ACCOUNT_USER;
 import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;
 import java.util.Map;

 import org.junit.jupiter.api.Assertions;
 import org.junit.jupiter.api.DisplayName;
 import org.junit.jupiter.api.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.ObjectWriter;
 import com.ibm.sdwan.viptela.driver.SdwanResponseException;
 import com.ibm.sdwan.viptela.model.viptela.DeviceDetails;

 @SpringBootTest
 public class PayloadConversionServiceTest {

 	@Autowired
 	private PayloadConversionService payloadConversionService;
 	@Mock
 	private ObjectMapper mapper;


 	@Test
 	@DisplayName("Testing positive scenario for SyncSmart")
 	public void buildPayloadForSyncSmartTest() throws JsonProcessingException {
 		String expected = "{\n"
 				+ "  \"username\" : \"test\",\n"
 				+ "  \"password\" : \"test\",\n"
 				+ "  \"validity_string\" : \"valid\"\n"
 				+ "}";
 		Map<String, Object> properties = new HashMap<String, Object>();
 		properties.put(SMART_ACCOUNT_USER, "test");
 		properties.put(SMART_ACCOUNT_PASSWORD, "test");
 		String result = payloadConversionService.buildPayloadForSyncSmart(properties);
 		Assertions.assertEquals(expected,result);
 	}

 	@Test 
 	@DisplayName("Testing positive scenario for Extracting Devices From Response")
 	public void extractDevicesFromResponseTest() {
 		DeviceDetails details = payloadConversionService
 				.extractDevicesFromResponse("{\"data\":[{\"deviceType\":\"type\",\"serialNumber\":\"1234asd\"}]}");
 		Assertions.assertNotNull(details);
 		Assertions.assertEquals("1234asd", details.getData().get(0).getSerialNumber());
 		Assertions.assertEquals("type", details.getData().get(0).getDeviceType());
 	}

 }
