package com.ibm.sdwan.viptela.driver;

import com.ibm.sdwan.viptela.model.ResourceManagerDeploymentLocation;
import com.ibm.sdwan.viptela.model.viptela.MessageDirection;
import com.ibm.sdwan.viptela.model.viptela.MessageType;
import com.ibm.sdwan.viptela.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ibm.sdwan.viptela.config.SDWDriverConstants.RC_SERVER_URL;
import static com.ibm.sdwan.viptela.utils.Constants.*;

@Component
public class SDWanDriver {
    private final static Logger logger = LoggerFactory.getLogger(SDWanDriver.class);

    private static final String API_SYNC_SMART_ENDPOINT = "/system/device/smartaccount/sync";
    private static final String API_GET_UUID = "/system/device/vedges?model=vedge-cloud";
    private static final String API_ATTACH_DEVICE_ENDPOINT = "/template/device/config/attachfeature";
    private static final String API_ATTACH_DEVICE_STATUS_ENDPOINT = "/device/action/status";
    private static final String API_GET_BOOTSTRAP = "/system/device/bootstrap/device";
    private static final String API_DECOMMISSION_ENDPOINT = "/system/device/decommission";

    private final RestTemplate restTemplate;

    @Autowired
    public SDWanDriver(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    /**
     * Utility method to create URL and calls the execute API.
     * @param lifecycleName cp4na lifecycle to be executed
     * @param deploymentLocation deployment location
     * @param payload payload to execute API
     * @param operation http operation that need to be run for the API
     * @param uuid deviceUuid required for certain API calls
     * @param veloCloudOperation operation that needs to be run within a lifecycle if any
     * @param jSessionId JSESSIONID that needs to be using in HTTP headers
     * @param xsrfToken XSRF token that needs to be using in HTTP headers
     * @return response
     */
    public String execute(String lifecycleName, ResourceManagerDeploymentLocation deploymentLocation, String payload, HttpMethod operation, String uuid, String veloCloudOperation, String jSessionId, String xsrfToken, String driverRequestId) {
        final String url = getURL(lifecycleName, deploymentLocation.getProperties(), uuid, veloCloudOperation);
        try {
            return executeAPI(url, payload, operation, jSessionId, xsrfToken, driverRequestId);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
            throw new SdwanResponseException("Issue with trusting SSL certificates.");
        }
    }

    /**
     * Executes the API with the HTTP method, payload and other required auth configurations.
     * @param url url of the API to be executed
     * @param payload payload to be used for POST if provided
     * @param operation http operation that need to be run for the API
     * @param jSessionId JSESSIONID that needs to be using in HTTP headers
     * @param xsrfToken XSRF token that needs to be using in HTTP headers
     * @return response
     */
    private String executeAPI(String url, String payload, HttpMethod operation, String jSessionId, String xsrfToken, String driverRequestId) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final HttpHeaders headers = getHttpHeaders(jSessionId, xsrfToken);
        final HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
        UUID uuid = UUID.randomUUID();
        Map<String, Object> metaInfoMap = new HashMap<>();
        metaInfoMap.put(URL, url);
        metaInfoMap.put(HTTP_METHOD, operation);
        final ResponseEntity<String> responseEntity;
        LogUtils.logEnabledMDC(payload, MessageType.REQUEST, MessageDirection.SENT, uuid.toString(), MediaType.APPLICATION_JSON_VALUE, PROTOCOL_TYPE, getRequestProtocolMetaData(url) ,driverRequestId);
        responseEntity = restTemplate.exchange(url, operation, requestEntity, String.class);
        LogUtils.logEnabledMDC(responseEntity.getBody(), MessageType.RESPONSE, MessageDirection.RECEIVED, uuid.toString(), MediaType.APPLICATION_JSON_VALUE, PROTOCOL_TYPE, getProtocolMetaData(url,responseEntity), driverRequestId);
        checkResponseEntityMatches(responseEntity, HttpStatus.OK, true);
        return responseEntity.getBody();
    }

    /**
     * Prepares and returns URL based on the lifecycle and its operations if any.
     * @param lifecycleName lifecycle name
     * @param deploymentLocationProperties deployment location
     * @param uuid uuid of the device
     * @param viptelaOperation operation within lifecycle if any
     * @return url
     */
    private String getURL(String lifecycleName, Map<String, Object> deploymentLocationProperties, String uuid, String viptelaOperation) {
        switch (lifecycleName) {
            case LIFECYCLE_CREATE:
                switch (viptelaOperation){
                    case SYNC_SMART:
                        return deploymentLocationProperties.get(RC_SERVER_URL) + API_CONTEXT + API_SYNC_SMART_ENDPOINT;
                    case GET_UUID:
                        return deploymentLocationProperties.get(RC_SERVER_URL) + API_CONTEXT + API_GET_UUID;
                    case ATTACH_DEVICE:
                        return deploymentLocationProperties.get(RC_SERVER_URL) + API_CONTEXT + API_ATTACH_DEVICE_ENDPOINT;
                    case ATTACH_DEVICE_STATUS:
                        return deploymentLocationProperties.get(RC_SERVER_URL) + API_CONTEXT + API_ATTACH_DEVICE_STATUS_ENDPOINT+ SLASH+ uuid;
                }
            case LIFECYCLE_INSTALL:
                return deploymentLocationProperties.get(RC_SERVER_URL) + API_CONTEXT + API_GET_BOOTSTRAP + SLASH + uuid + CONFIG_TYPE_PATH;
            case LIFECYCLE_DELETE:
                return deploymentLocationProperties.get(RC_SERVER_URL) + API_CONTEXT + API_DECOMMISSION_ENDPOINT + SLASH + uuid;
            default:
                throw new IllegalArgumentException(String.format("Requested transition [%s] is not supported by this lifecycle driver", lifecycleName));
        }
    }

    /**
     * Creates and returns HTTP headers, populating the content type (as application/json along with cookie & token)
     * @param jSessionId JSESSIONID that needs to be set in http headers
     * @param xsrfToken XSRF Token that needs to be set in http headers
     * @return HttpHeaders
     */
    private HttpHeaders getHttpHeaders(String jSessionId, String xsrfToken) throws SdwanResponseException {
        final HttpHeaders apiHeaders = new HttpHeaders();
        apiHeaders.setContentType(MediaType.APPLICATION_JSON);
        apiHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        apiHeaders.set("Cookie", "JSESSIONID="+jSessionId);
        apiHeaders.set("X-XSRF-TOKEN", xsrfToken);
        return apiHeaders;
    }

    /**
     * Utility method that checks if the HTTP status code matches the expected value and that it contains a response body (if desired)
     *
     * @param responseEntity       response to check
     * @param expectedStatusCode   HTTP status code to check against
     * @param containsResponseBody whether the response should contain a body
     */
    private void checkResponseEntityMatches(final ResponseEntity<String> responseEntity, final HttpStatus expectedStatusCode, final boolean containsResponseBody) throws SdwanResponseException {
        // Check response code matches expected value (log a warning if incorrect 2xx status seen)
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getStatusCode() != expectedStatusCode) {
            // Be lenient on 2xx response codes
            logger.warn("Invalid status code [{}] received, was expecting [{}]", responseEntity.getStatusCode(), expectedStatusCode);
        } else if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new SdwanResponseException(String.format("Invalid status code [%s] received", responseEntity.getStatusCode()));
        }
        // Check if the response body is populated (or not) as expected
        if (containsResponseBody && responseEntity.getBody() == null) {
            throw new SdwanResponseException("No response body");
        } else if (!containsResponseBody && responseEntity.getBody() != null) {
            throw new SdwanResponseException("No response body expected");
        }
    }

    Map<String,Object> getProtocolMetaData(String url,ResponseEntity responseEntity){

        Map<String,Object> protocolMetadata=new HashMap<>();

        protocolMetadata.put("status",responseEntity.getStatusCode());
        protocolMetadata.put("status_code",responseEntity.getStatusCodeValue());
        protocolMetadata.put("url",url);

        return protocolMetadata;

    }

    Map<String,Object> getRequestProtocolMetaData(String url){

        Map<String,Object> protocolMetadata=new HashMap<>();
        protocolMetadata.put("url",url);
        return protocolMetadata;
    }
}
