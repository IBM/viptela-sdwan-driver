package com.ibm.sdwan.viptela.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.ibm.sdwan.viptela.config.SDWDriverConstants;
import com.ibm.sdwan.viptela.config.SDWDriverProperties;
import com.ibm.sdwan.viptela.model.ExecutionRequest;
import com.ibm.sdwan.viptela.model.ResourceManagerDeploymentLocation;
import com.ibm.sdwan.viptela.service.MissingPropertyException;

@SpringBootTest
public class ViptelaUtilsTest {

	@Test
	@DisplayName("Testing positive scenario for Delay vManageApiRetry Delay as String")
	public void getDelay_vManageApiRetryDelay_StringTest() {
		ExecutionRequest executionRequest = new ExecutionRequest();
		Map<String, Object> dummyDepLocProperties = new HashMap<>();
		dummyDepLocProperties.put("vManageApiRetryDelay", "12");
		ResourceManagerDeploymentLocation deploymentLocation = new ResourceManagerDeploymentLocation();
		deploymentLocation.setProperties(dummyDepLocProperties);
		executionRequest.setDeploymentLocation(deploymentLocation);
		SDWDriverProperties sdwDriverProperties = new SDWDriverProperties();
		sdwDriverProperties.setvManageApiDelay(Duration.ofSeconds(1));
		long delay = ViptelaUtils.getDelay(sdwDriverProperties, executionRequest);
		assertEquals(12*1000, delay);
	}

	@Test
	@DisplayName("Testing negative scenario for Delay vManageApiRetry Delay as Long")
	public void getDelay_vManageApiRetryDelay_longTest() {
		ExecutionRequest executionRequest = new ExecutionRequest();
		Map<String, Object> dummyDepLocProperties = new HashMap<>();
		dummyDepLocProperties.put("vManageApiRetryDelay", 1L);
		ResourceManagerDeploymentLocation deploymentLocation = new ResourceManagerDeploymentLocation();
		deploymentLocation.setProperties(dummyDepLocProperties);
		executionRequest.setDeploymentLocation(deploymentLocation);
		SDWDriverProperties sdwDriverProperties = new SDWDriverProperties();
		sdwDriverProperties.setvManageApiDelay(Duration.ofSeconds(1));
		long delay = ViptelaUtils.getDelay(sdwDriverProperties, executionRequest);
		assertEquals(1*1000, delay);
	}

	@Test
	@DisplayName("Testing positive scenario for validating DeploymentProperties")
	public void validateDeploymentPropertiesTest() {
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(null);
		});

		Map<String, Object> deploymentLocationProperties = new HashMap<String, Object>();

		deploymentLocationProperties.put(SDWDriverConstants.RC_SERVER_URL, null);
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(SDWDriverConstants.RC_SERVER_URL, "");
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(SDWDriverConstants.RC_SERVER_URL, "rcServerURL");
		deploymentLocationProperties.put(Constants.V_MANAGE_USER, "");
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(Constants.V_MANAGE_USER, null);
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(Constants.V_MANAGE_USER, "vManageUser");
		deploymentLocationProperties.put(Constants.V_MANAGE_PASSWORD, "");
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(Constants.V_MANAGE_PASSWORD, null);
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(Constants.V_MANAGE_PASSWORD, "vmanagePassword");
		deploymentLocationProperties.put(Constants.SMART_ACCOUNT_USER, "");
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(Constants.SMART_ACCOUNT_USER, null);
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(Constants.SMART_ACCOUNT_USER, "smartAccountUser");
		deploymentLocationProperties.put(Constants.SMART_ACCOUNT_PASSWORD, "");
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

		deploymentLocationProperties.put(Constants.SMART_ACCOUNT_PASSWORD, null);
		assertThrows(MissingPropertyException.class, () -> {
			ViptelaUtils.validateDeploymentProperties(deploymentLocationProperties);
		});

	}

}
