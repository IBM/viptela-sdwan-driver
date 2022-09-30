package com.ibm.sdwan.viptela.driver;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.ibm.sdwan.viptela.config.SDWDriverConstants;
import com.ibm.sdwan.viptela.model.ResourceManagerDeploymentLocation;
import com.ibm.sdwan.viptela.utils.Constants;

@SpringBootTest
public class SDWanDriverTest {

	private SDWanDriver sdWanDriver;
	@Mock
	private RestTemplate restTemplate;

	Map<String, Object> deploymentLocationProperties;
	ResourceManagerDeploymentLocation deploymentLocation;

	@BeforeEach
	public void setUp() {
		sdWanDriver = new SDWanDriver(restTemplate);
		deploymentLocation = new ResourceManagerDeploymentLocation();
		deploymentLocationProperties = new HashMap<String, Object>();
		deploymentLocationProperties.put(SDWDriverConstants.RC_SERVER_URL, "http://localhost:8080");

		deploymentLocation.setProperties(deploymentLocationProperties);
	}

	@Test
	@DisplayName("Testing positive scenario to Execute URL for LIFECYCLE_CREATE with SyncSmart")
	public void executeURLforLIFECYCLE_CREATE_With_SyncSmart() {
		String url = "http://localhost:8080/dataservice/system/device/smartaccount/sync";
		ResponseEntity<String> response = new ResponseEntity<String>("test", HttpStatus.OK);
		ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), eq(String.class)))
				.thenReturn(response);

		String res = sdWanDriver.execute(Constants.LIFECYCLE_CREATE, deploymentLocation, url, HttpMethod.POST,
				UUID.randomUUID().toString(), "SyncSmart", "", "", "");
		assertNotNull(res);
	}

	@Test
	@DisplayName("Testing positive scenario to Execute URL for LIFECYCLE_CREATE with Get_UUID")
	public void executeURLforLIFECYCLE_CREATE_With_Get_UUID() {
		String url = "http://localhost:8080/dataservice/system/device/vedges?model=vedge-cloud";
		ResponseEntity<String> response = new ResponseEntity<String>("test", HttpStatus.OK);
		ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), eq(String.class)))
				.thenReturn(response);

		String res = sdWanDriver.execute(Constants.LIFECYCLE_CREATE, deploymentLocation, url, HttpMethod.POST,
				UUID.randomUUID().toString(), Constants.GET_UUID, "", "", "");
		assertNotNull(res);
	}

	@Test
	@DisplayName("Testing positive scenario to Execute URL for LIFECYCLE_CREATE with ATTACH_DEVICE")
	public void executeURLforLIFECYCLE_CREATE_With_ATTACH_DEVICE() {
		String url = "http://localhost:8080/dataservice/template/device/config/attachfeature";
		ResponseEntity<String> response = new ResponseEntity<String>("test", HttpStatus.OK);
		ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), eq(String.class)))
				.thenReturn(response);

		String res = sdWanDriver.execute(Constants.LIFECYCLE_CREATE, deploymentLocation, url, HttpMethod.POST,
				UUID.randomUUID().toString(), Constants.ATTACH_DEVICE, "", "", "");
		assertNotNull(res);
	}

	@Test
	@DisplayName("Testing positive scenario to Execute URL for LIFECYCLE_INSTALL")
	public void executeURLforLIFECYCLE_INSTALL() {
		String url = "http://localhost:8080/dataservice/system/device/bootstrap/device/test-uuid?configtype=cloudinit&inclDefRootCert=false";
		ResponseEntity<String> response = new ResponseEntity<String>("test", HttpStatus.OK);
		ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(String.class)))
				.thenReturn(response);

		String res = sdWanDriver.execute(Constants.LIFECYCLE_INSTALL, deploymentLocation, url, HttpMethod.GET,
				"test-uuid", "", "", "", "");
		assertNotNull(res);
	}

	@Test
	@DisplayName("Testing positive scenario to Execute URL for LIFECYCLE_DELETE")
	public void executeURLforLIFECYCLE_DELETE() {
		String url = "http://localhost:8080/dataservice/system/device/decommission/test-uuid";
		ResponseEntity<String> response = new ResponseEntity<String>("test", HttpStatus.ACCEPTED);
		ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(String.class)))
				.thenReturn(response);

		String res = sdWanDriver.execute(Constants.LIFECYCLE_DELETE, deploymentLocation, url, HttpMethod.GET,
				"test-uuid", "", "", "", "");
		assertNotNull(res);
	}

	@Test
	@DisplayName("Testing exception scenario to Execute URL for LIFECYCLE_DELETE ")
	public void executeURLforLIFECYCLE_DELETE_Exception() {
		String url = "http://localhost:8080/dataservice/system/device/decommission/test-uuid";
		String resp = null;
		ResponseEntity<String> response = new ResponseEntity<String>("test", HttpStatus.BAD_REQUEST);
		ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(String.class)))
				.thenReturn(response);

		assertThrows(SdwanResponseException.class, () -> {
			sdWanDriver.execute(Constants.LIFECYCLE_DELETE, deploymentLocation, url, HttpMethod.GET, "test-uuid", "",
					"", "", "");

		});
	}

	@Test
	@DisplayName("Testing positive scenario to Execute URL for LIFECYCLE UNKNOWN")
	public void executeURLforLIFECYCLE_UNKNOWN_Exception() {
		String url = "http://localhost:8080/dataservice/system/device/decommission/test-uuid";
		String resp = null;
		ResponseEntity<String> response = new ResponseEntity<String>("test", HttpStatus.BAD_REQUEST);
		ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), httpEntityArgumentCaptor.capture(), eq(String.class)))
				.thenReturn(response);

		assertThrows(IllegalArgumentException.class, () -> {
			sdWanDriver.execute("UNKNOWN", deploymentLocation, url, HttpMethod.GET, "test-uuid", "", "", "", "");

		});
	}
}
