package com.ibm.sdwan.viptela.service.impl;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sdwan.viptela.config.SDWDriverProperties;
import com.ibm.sdwan.viptela.driver.SdwanResponseException;
import com.ibm.sdwan.viptela.model.viptela.KafkaMessage;

@SpringBootTest
public class KafkaInternalMessagingServiceImplTest {

	@Autowired
	private  SDWDriverProperties properties;
	@Mock
    private  KafkaTemplate<String, String> kafkaTemplate;
	@Mock
    private  ObjectMapper objectMapper;
	
	private KafkaInternalMessagingServiceImpl kafkaInternalMessagingServiceImpl;
	
	@BeforeEach
	public void setup() {
		kafkaInternalMessagingServiceImpl=new KafkaInternalMessagingServiceImpl(properties, kafkaTemplate, objectMapper);
	}
	
	@Test
 	@DisplayName("Testing positive scenario to Send Delayed Execution Async Response")
    public void  sendEdgeStatusAsyncResponse() throws JsonProcessingException {
		KafkaMessage kafkaMessage = new KafkaMessage();
		
    	SendResult<String, Object> sendResult = mock(SendResult.class);
 		ListenableFuture<SendResult<String, String>> responseFuture = mock(ListenableFuture.class);
 		Mockito.when(objectMapper.writeValueAsString(any())).thenReturn("test");
 		Mockito.when(kafkaTemplate.send(Mockito.anyString(), Mockito.anyString())).thenReturn(responseFuture);
    	kafkaInternalMessagingServiceImpl.sendVmanageApiAsyncResponse(kafkaMessage);
    	verify(kafkaTemplate, times(1)).send(Mockito.anyString(), Mockito.anyString());
   }
	 
	@Test
 	@DisplayName("Testing exception scenario for Async Response--JsonProcessingException")
    public void  sendEdgeStatusAsyncResponseexception() throws SdwanResponseException, JsonProcessingException {
    	SendResult<String, Object> sendResult = mock(SendResult.class);
 		ListenableFuture<SendResult<String, String>> responseFuture = mock(ListenableFuture.class);
 		Mockito.when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
 		assertThrows(SdwanResponseException.class, () -> {
 			kafkaInternalMessagingServiceImpl.sendVmanageApiAsyncResponse(null); 
 		});
 		
   }
    
}
