package com.ibm.sdwan.viptela.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sdwan.viptela.config.SDWDriverProperties;
import com.ibm.sdwan.viptela.driver.SdwanResponseException;
import com.ibm.sdwan.viptela.model.ExecutionRequest;
import com.ibm.sdwan.viptela.model.ResourceManagerDeploymentLocation;
import com.ibm.sdwan.viptela.model.viptela.KafkaMessage;
import com.ibm.sdwan.viptela.service.LifecycleManagementService;
import com.ibm.sdwan.viptela.service.MessageConversionException;

@SpringBootTest
public class VManageApiRetryServiceListenerTest {

	private VManageApiRetryServiceListener vManageApiRetryServiceListener;
	@Mock
	private SDWDriverProperties sdwDriverProperties;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private LifecycleManagementService lifecycleManagementService;
	

	@BeforeEach
	public void setUp() {

		vManageApiRetryServiceListener = new VManageApiRetryServiceListener(sdwDriverProperties, objectMapper,
				lifecycleManagementService);

	}

	@Test
	@DisplayName("Testing positive scenario for Listener For VManageApi  Status FAILED")
	public void testListenerForVManageApiRetryFailed() throws MessageConversionException, JsonMappingException, JsonProcessingException {
		KafkaMessage kafkaMessage = new KafkaMessage();
		kafkaMessage.setStatus("FAILED");
		ExecutionRequest executionRequest = new ExecutionRequest();
		ResourceManagerDeploymentLocation deploymentLocation = new ResourceManagerDeploymentLocation();
		deploymentLocation.getProperties().put("vManageApiRetryDelay", "1");
		executionRequest.setDeploymentLocation(deploymentLocation);
		kafkaMessage.setExecutionRequest(executionRequest);
		when(objectMapper.readValue("test",KafkaMessage.class)).thenReturn(kafkaMessage);
		
		vManageApiRetryServiceListener.listenForVManageApiRetryMessages("test");
		verify(lifecycleManagementService, times(1)).runLifecycle(null,executionRequest,-1, null);
	} 
	
	@Test
	@DisplayName("Testing negative scenario for Listener For VManageApi  Status SUCCESS")
	public void testListenerForVManageApiRetryFailed1() throws MessageConversionException, JsonMappingException, JsonProcessingException {
		KafkaMessage kafkaMessage = new KafkaMessage();
		kafkaMessage.setStatus("SUCCESS");
		ExecutionRequest executionRequest = new ExecutionRequest();
		ResourceManagerDeploymentLocation deploymentLocation = new ResourceManagerDeploymentLocation();
		deploymentLocation.getProperties().put("vManageApiRetryDelay", "1");
		executionRequest.setDeploymentLocation(deploymentLocation);
		kafkaMessage.setExecutionRequest(executionRequest);
		when(objectMapper.readValue("test",KafkaMessage.class)).thenReturn(kafkaMessage);
		assertThrows(SdwanResponseException.class, () -> {
			vManageApiRetryServiceListener.listenForVManageApiRetryMessages("test");			
		});

	}
	
	@Test
	@DisplayName("Testing Exception scenario  for Listener For VManageApi Status Default")
	public void testListenerForVManageApiRetryFailed2() throws MessageConversionException, JsonMappingException, JsonProcessingException {
	
		when(objectMapper.readValue("test",KafkaMessage.class)).thenThrow(JsonProcessingException.class);
		assertThrows(SdwanResponseException.class, () -> {
			vManageApiRetryServiceListener.listenForVManageApiRetryMessages("test");			
		});

	}

}

