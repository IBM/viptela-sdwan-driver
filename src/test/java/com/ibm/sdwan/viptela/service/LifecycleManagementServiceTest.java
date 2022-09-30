 package com.ibm.sdwan.viptela.service;

 import static com.ibm.sdwan.viptela.utils.Constants.SYNC_SMART;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;

import com.ibm.sdwan.viptela.config.SDWDriverProperties;
import com.ibm.sdwan.viptela.driver.SDWanDriver;
import com.ibm.sdwan.viptela.driver.SdwanResponseException;
import com.ibm.sdwan.viptela.model.ExecutionAcceptedResponse;
import com.ibm.sdwan.viptela.model.ExecutionRequest;
import com.ibm.sdwan.viptela.model.GenericExecutionRequestPropertyValue;
import com.ibm.sdwan.viptela.model.ResourceManagerDeploymentLocation;
import com.ibm.sdwan.viptela.model.viptela.DeviceData;
import com.ibm.sdwan.viptela.model.viptela.DeviceDetails;
import com.ibm.sdwan.viptela.security.AuthenticationService;
//import com.ibm.sdwan.viptela.security.AuthenticatedRestTemplateService;
 import com.ibm.sdwan.viptela.utils.Constants;

 @SpringBootTest
 public class LifecycleManagementServiceTest {

 	private LifecycleManagementService lifecycleManagementService;
 	private ExecutionRequest executionRequest;
 	@Mock
 	private SDWanDriver sdwanDriver;
 	@Mock
     private MessageConversionService messageConversionService;
 	@Mock
     private PayloadConversionService payloadConversionService;
 	@Mock
     private ExternalMessagingService externalMessagingService;
 	@Mock
     private SDWDriverProperties rcDriverProperties;
 	//@Mock
    // private AuthenticatedRestTemplateService authenticatedRestTemplateService;
 	@Mock
     private InternalMessagingService internalMessagingService;
 	@Mock
 	private  AuthenticationService authenticationService;

	 private String tenantId;
 	@BeforeEach
 	public void setUp() {
 		lifecycleManagementService = new LifecycleManagementService(sdwanDriver, messageConversionService, payloadConversionService, externalMessagingService,rcDriverProperties, authenticationService,internalMessagingService);
 		executionRequest = new ExecutionRequest();
 		Map<String, Object> dummyDepLocProperties = new HashMap<>();
 		dummyDepLocProperties.put("vManageHostFqdn", "dummy");
		dummyDepLocProperties.put("vManageUser", "dummy");
		dummyDepLocProperties.put("vManagePassword", "dummy");
		dummyDepLocProperties.put("smartAccountUser", "dummy");
		dummyDepLocProperties.put("smartAccountPassword", "dummy");
 		dummyDepLocProperties.put("vManageApiRetryCount", "1");
 		ResourceManagerDeploymentLocation deploymentLocation = new ResourceManagerDeploymentLocation();
 		deploymentLocation.setProperties(dummyDepLocProperties);
 		executionRequest.setDeploymentLocation(deploymentLocation);
 	}

 	@Test
 	@DisplayName("Testing positive scenario for Create Lifecycle")
 	public void executeLifecycleCreateTest() throws MessageConversionException {
 		executionRequest.setLifecycleName(Constants.LIFECYCLE_CREATE); 
 		DeviceData deviceData = new DeviceData();
 		deviceData.setDeviceModel(Constants.VEDGE_CLOUD);
 		deviceData.setUuid(UUID.randomUUID().toString());
 		ArrayList<DeviceData> list = new ArrayList<DeviceData>(); 
 		list.add(deviceData);
 		DeviceDetails deviceDetails = new DeviceDetails();
 		deviceDetails.setData(list);
 		String uuid = UUID.randomUUID().toString();
 		Mockito.when(payloadConversionService.extractDevicesFromResponse(Mockito.any())).thenReturn(deviceDetails);
 		Mockito.when(payloadConversionService
 				.buildPayloadForSyncSmart(executionRequest.getDeploymentLocation().getProperties())).thenReturn("test");
		
 		Mockito.when(sdwanDriver.execute(Constants.LIFECYCLE_CREATE, executionRequest.getDeploymentLocation(), "", HttpMethod.POST, "", SYNC_SMART, "", "", "")).thenReturn(uuid);
 		ExecutionAcceptedResponse result = lifecycleManagementService.executeLifecycle(executionRequest,tenantId);
 		assertNotNull(result);
 		assertNotNull(result.getRequestId());
 	}
 	
 	@Test
 	@DisplayName("Testing positive scenario for Create Lifecycle - count is zero")
 	public void executeLifecycleCreateTestCountIsZero() throws MessageConversionException {
 		executionRequest.getDeploymentLocation().getProperties().put("vManageApiRetryCount", 0);
 		executionRequest.setLifecycleName(Constants.LIFECYCLE_CREATE);
 		DeviceData deviceData = new DeviceData();
 		deviceData.setDeviceModel(Constants.VEDGE_CLOUD);
 		deviceData.setUuid(UUID.randomUUID().toString());
 		ArrayList<DeviceData> list = new ArrayList<DeviceData>(); 
 		list.add(deviceData);
 		DeviceDetails deviceDetails = new DeviceDetails();
 		deviceDetails.setData(list);
 		String uuid = UUID.randomUUID().toString();
 		Mockito.when(payloadConversionService.extractDevicesFromResponse(Mockito.any())).thenReturn(deviceDetails);
 		Mockito.when(payloadConversionService
 				.buildPayloadForSyncSmart(executionRequest.getDeploymentLocation().getProperties())).thenReturn("test");
		
 		Mockito.when(sdwanDriver.execute(Constants.LIFECYCLE_CREATE, executionRequest.getDeploymentLocation(), "", HttpMethod.POST, "", SYNC_SMART, "", "","")).thenReturn(uuid);
 		assertThrows(SdwanResponseException.class, () -> {
 			lifecycleManagementService.executeLifecycle(executionRequest,tenantId);
 		});
 	
 	}
 	
 	@Test
 	@DisplayName("Testing positive scenario for Create Lifecycle - authenticationService failure")
 	public void executeLifecycleCreateTestAuthenticationServiceFailure() throws MessageConversionException {
 		executionRequest.getDeploymentLocation().getProperties().put("vManageApiRetryCount", "1");
 		executionRequest.setLifecycleName(Constants.LIFECYCLE_CREATE);
 		DeviceData deviceData = new DeviceData();
 		deviceData.setDeviceModel(Constants.VEDGE_CLOUD);
 		deviceData.setUuid(UUID.randomUUID().toString());
 		ArrayList<DeviceData> list = new ArrayList<DeviceData>(); 
 		list.add(deviceData);
 		DeviceDetails deviceDetails = new DeviceDetails();
 		deviceDetails.setData(list);
 		String uuid = UUID.randomUUID().toString();
 		Mockito.when(payloadConversionService.extractDevicesFromResponse(Mockito.any())).thenReturn(deviceDetails);
 		Mockito.when(payloadConversionService
 				.buildPayloadForSyncSmart(executionRequest.getDeploymentLocation().getProperties())).thenReturn("test");
		Mockito.when(authenticationService.getJsessionId(Mockito.anyMap())).thenThrow(ResourceAccessException.class);
 		Mockito.when(sdwanDriver.execute(Constants.LIFECYCLE_CREATE, executionRequest.getDeploymentLocation(), "", HttpMethod.POST, "", SYNC_SMART, "", "","")).thenReturn(uuid);
 	
 		ExecutionAcceptedResponse result = lifecycleManagementService.executeLifecycle(executionRequest,tenantId);
 		assertNotNull(result);
 		assertNotNull(result.getRequestId());
 	
 	}


 	@Test
 	@DisplayName("Testing exception scenario for Create lifecycle - Device Model other than VEDGE_CLOUD ")
 	public void executeLifecycleCreateDeviceModelOtherThanVedgeCloudTest() throws MessageConversionException {
 		executionRequest.setLifecycleName(Constants.LIFECYCLE_CREATE);
 		DeviceData deviceData = new DeviceData();
 		deviceData.setDeviceModel(Constants.GET_UUID);
 		deviceData.setDeviceIP("127.0.0.1");
 		deviceData.setUuid(UUID.randomUUID().toString());
 		ArrayList<DeviceData> list = new ArrayList<DeviceData>();
 		list.add(deviceData);
 		DeviceDetails deviceDetails = new DeviceDetails();
 		deviceDetails.setData(list);
 		Mockito.when(payloadConversionService.extractDevicesFromResponse(Mockito.any())).thenReturn(deviceDetails);
 		assertThrows(SdwanResponseException.class, () -> {
 			lifecycleManagementService.executeLifecycle(executionRequest,tenantId);
 		});
 	}

 	@Test
 	@DisplayName("Testing positive scenario for Install Lifecycle")
 	public void executeLifecycleInstallTest() throws MessageConversionException {
 		executionRequest.setLifecycleName(Constants.LIFECYCLE_INSTALL);
 		ExecutionAcceptedResponse result = lifecycleManagementService.executeLifecycle(executionRequest,tenantId);
 		assertNotNull(result);
 	}

 	@Test
 	@DisplayName("Testing positive scenario for Delete Lifecycle")
 	public void executeLifecycleDeleteWithUUIDTest() throws MessageConversionException {
 		executionRequest.setLifecycleName(Constants.LIFECYCLE_DELETE);
 		executionRequest.getResourceProperties().put(Constants.DEVICE_UUID,
 				new GenericExecutionRequestPropertyValue(UUID.randomUUID().toString()));
 		ExecutionAcceptedResponse result = lifecycleManagementService.executeLifecycle(executionRequest,tenantId);
 		assertNotNull(result);
 	}

 	@Test
 	@DisplayName("Testing exception scenario for Delete Lifecycle - missing property UUID")
 	public void executeLifecycleDeleteWithOutUUIDTest() throws MessageConversionException {
 		executionRequest.setLifecycleName(Constants.LIFECYCLE_DELETE);
 		ExecutionAcceptedResponse result = lifecycleManagementService.executeLifecycle(executionRequest,tenantId);
 		assertNotNull(result);
 		assertNotNull(result.getRequestId());
 	}

 	@Test
 	@DisplayName("Testing positive scenario for Default Lifecycle ")
 	public void executeLifecycleDefaultTest() throws MessageConversionException {
 		executionRequest.setLifecycleName(Constants.ATTACH_DEVICE);
 		assertThrows(IllegalArgumentException.class, () -> {
 			lifecycleManagementService.executeLifecycle(executionRequest,tenantId);
 		});
 	}

 }