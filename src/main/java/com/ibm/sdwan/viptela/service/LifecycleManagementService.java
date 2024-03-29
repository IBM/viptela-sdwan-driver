package com.ibm.sdwan.viptela.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.sdwan.viptela.config.SDWDriverProperties;
import com.ibm.sdwan.viptela.driver.SDWanDriver;
import com.ibm.sdwan.viptela.driver.SdwanResponseException;
import com.ibm.sdwan.viptela.model.ExecutionAcceptedResponse;
import com.ibm.sdwan.viptela.model.ExecutionRequest;
import com.ibm.sdwan.viptela.model.ExecutionRequestPropertyValue;
import com.ibm.sdwan.viptela.model.alm.ExecutionAsyncResponse;
import com.ibm.sdwan.viptela.model.alm.ExecutionStatus;
import com.ibm.sdwan.viptela.model.alm.FailureDetails;
import com.ibm.sdwan.viptela.model.alm.FailureDetails.FailureCode;
import com.ibm.sdwan.viptela.model.viptela.*;
import com.ibm.sdwan.viptela.security.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;
import java.util.stream.Collectors;

import static com.ibm.sdwan.viptela.utils.Constants.*;
import com.ibm.sdwan.viptela.utils.ViptelaUtils;

@Service("LifecycleManagementService")
public class LifecycleManagementService {

    private final static Logger logger = LoggerFactory.getLogger(LifecycleManagementService.class);
    private final SDWanDriver sdwanDriver;
    private final MessageConversionService messageConversionService;
    private final PayloadConversionService payloadConversionService;
    private final ExternalMessagingService externalMessagingService;
    private final SDWDriverProperties rcDriverProperties;
    private final AuthenticationService authenticationService;
    private final InternalMessagingService internalMessagingService;

    private int maxVManageApiRetryCount;
    @Autowired
    public LifecycleManagementService(SDWanDriver sdwanDriver, MessageConversionService messageConversionService,
            PayloadConversionService payloadConversionService, ExternalMessagingService externalMessagingService,
            SDWDriverProperties rcDriverProperties, AuthenticationService authenticationService,
            InternalMessagingService internalMessagingService) {
        this.sdwanDriver = sdwanDriver;
        this.messageConversionService = messageConversionService;
        this.payloadConversionService = payloadConversionService;
        this.externalMessagingService = externalMessagingService;
        this.rcDriverProperties = rcDriverProperties;
        this.authenticationService = authenticationService;
        this.internalMessagingService = internalMessagingService;
    }

    public ExecutionAcceptedResponse executeLifecycle(ExecutionRequest executionRequest, String tenantId) throws MessageConversionException {
        final String requestId = UUID.randomUUID().toString();
        ViptelaUtils.validateDeploymentProperties(executionRequest.getDeploymentLocation().getProperties());
        int vManageApiRetryCount = V_MANAGE_API_RETRY_COUNT;
        Object vManageApiRetryCountObject = executionRequest.getDeploymentLocation().getProperties()
                .get("vManageApiRetryCount");
        if (vManageApiRetryCountObject != null) {
            if (vManageApiRetryCountObject instanceof String) {
                vManageApiRetryCount = Integer.parseInt((String) vManageApiRetryCountObject);
            } else {
                vManageApiRetryCount = (int) vManageApiRetryCountObject;
            }
        }
        maxVManageApiRetryCount = vManageApiRetryCount;
        return runLifecycle(requestId, executionRequest, vManageApiRetryCount,tenantId);
    }

    public ExecutionAcceptedResponse runLifecycle(String requestId, ExecutionRequest executionRequest, int vManageApiRetryCount, String tenantId) throws MessageConversionException {
        if(vManageApiRetryCount == 0){
            long vManageApiRetryDelayInSecs = ViptelaUtils.getDelay(rcDriverProperties, executionRequest)/1000;
            logger.error("Viptela VManage is not reachable, retried connecting for "+maxVManageApiRetryCount+" times with delay of " + vManageApiRetryDelayInSecs +" seconds");
            FailureDetails failureDetails = new FailureDetails();
            failureDetails.setFailureCode(FailureCode.INFRASTRUCTURE_ERROR);
            failureDetails.setDescription("Viptela VManage is not reachable, retried connecting for "+maxVManageApiRetryCount+" times with delay of " + vManageApiRetryDelayInSecs +" seconds");
            externalMessagingService.sendDelayedExecutionAsyncResponse(new ExecutionAsyncResponse(requestId, ExecutionStatus.FAILED, failureDetails, null, Collections.emptyMap()), tenantId, rcDriverProperties.getExecutionResponseDelay());
            throw new SdwanResponseException("Viptela VManage is not reachable, retried connecting for "+maxVManageApiRetryCount+" times with delay of " + vManageApiRetryDelayInSecs +" seconds");
            
        }
        Map<String, String> authenticationProperties = executionRequest.getDeploymentLocation().getProperties()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
        String jsessionId = null;
        Map<String, Object> outputs = new HashMap<>();
        String lifecycleName = executionRequest.getLifecycleName();
        try {
            jsessionId = authenticationService.getJsessionId(authenticationProperties);
            //jsessionId = authenticationService.getJsessionId_error2(authenticationProperties,vManageApiRetryCount);
        } catch (ResourceAccessException e) {
           logger.info("j_security_check api is connection timed out, need to retry");
           KafkaMessage kafkaMessage = new KafkaMessage();
           kafkaMessage.setRequestId(requestId);
           kafkaMessage.setStatus("FAILED");
           kafkaMessage.setExecutionRequest(executionRequest);
           kafkaMessage.setVManageApiRetryCount(vManageApiRetryCount);
           kafkaMessage.setTenantId(tenantId);      
           internalMessagingService.sendVmanageApiAsyncResponse(kafkaMessage);
           return new ExecutionAcceptedResponse(requestId);
        }
        try {
            String xsrfToken = authenticationService.getXsrfToken(authenticationProperties, jsessionId);

            logger.info("Processing execution request");
            switch (lifecycleName) {
                case LIFECYCLE_CREATE:
                    // sync licences
                    String payloadForSyncSmart = payloadConversionService
                            .buildPayloadForSyncSmart(executionRequest.getDeploymentLocation().getProperties());
                    sdwanDriver.execute(lifecycleName, executionRequest.getDeploymentLocation(), payloadForSyncSmart,
                            HttpMethod.POST, "", SYNC_SMART, jsessionId, xsrfToken, requestId);
                    // Get uuid
                    String responseGetUUID = sdwanDriver.execute(lifecycleName,
                            executionRequest.getDeploymentLocation(), "", HttpMethod.GET, "", GET_UUID, jsessionId,
                            xsrfToken, requestId);
                    logger.info("responseGetUUID : " + responseGetUUID);
                    DeviceDetails deviceDetails = payloadConversionService.extractDevicesFromResponse(responseGetUUID);
                    List<DeviceData> deviceList = deviceDetails.getData();
                    // filter the device model of tyoe 'vedge-cloud' and with device-ip is null or empty
                    Optional<DeviceData> deviceOptional = deviceList.stream()
                            .filter((deviceData) -> deviceData.getDeviceModel().equalsIgnoreCase(VEDGE_CLOUD)
                                    && deviceData.getDeviceIP() == null
                                    || !StringUtils.hasLength(deviceData.getDeviceIP()))
                            .findFirst();
                    DeviceData deviceFound;
                    if (deviceOptional.isPresent()) {
                        deviceFound = deviceOptional.get();
                    } else {
                        authenticationService.logout(authenticationProperties, jsessionId);
                        throw new SdwanResponseException("Could not find any device with empty deviceIP");
                    }
                    // find the uuid of the device, which is used to decommission it.
                    String uuid = deviceFound.getUuid();
                    logger.info("uuid of the device to respond back: " + uuid);
                    ExecutionRequestPropertyValue propertyValue = new ExecutionRequestPropertyValue() {
                        @Override
                        public Object getValue() {
                            return uuid;
                        }
                    };
                    executionRequest.getResourceProperties().put(DEVICE_UUID, propertyValue);
                    // attach template to a device
                    String payload = messageConversionService.generateMessageFromRequest(ATTACH_DEVICE,
                            executionRequest);
                    String output = sdwanDriver.execute(lifecycleName, executionRequest.getDeploymentLocation(), payload,
                            HttpMethod.POST, "", ATTACH_DEVICE, jsessionId, xsrfToken, requestId);
                    AttachDeviceResponse attachDeviceResponse;
                    if(output==null) {
                        authenticationService.logout(authenticationProperties, jsessionId);
                        throw new SdwanResponseException("No response received after attaching the device.");
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        attachDeviceResponse = mapper.readValue(output, AttachDeviceResponse.class);
                    } catch (JsonProcessingException e) {
                        authenticationService.logout(authenticationProperties, jsessionId);
                        throw new SdwanResponseException(e.getMessage());
                    }
                    logger.info(attachDeviceResponse.getId());
                    outputs.put(DEVICE_UUID, uuid);
                    // check the status of the attached device.
                    // checks the status with delay of 5 seconds for 12 attempts (It might need minimum 2 minutes of timeout value specified
                    // for Create lifecycle transition in the resource descriptor in case of Brent timeout error)
                    int retryCount = 1;
                    DeviceStatus deviceStatus;
                    boolean deviceAttachStatus = false;
                    while( retryCount <= STATUS_CHECK_RETRY_COUNT){
                        logger.info("Attempting to get attach device status, attempt number: "+ retryCount);
                        String responseData = sdwanDriver.execute(lifecycleName, executionRequest.getDeploymentLocation(), "",
                                HttpMethod.GET, attachDeviceResponse.getId(), ATTACH_DEVICE_STATUS, jsessionId, xsrfToken, requestId);
                        if(responseData==null) {
                            authenticationService.logout(authenticationProperties, jsessionId);
                            throw new SdwanResponseException("No response received while checking the status of attaching template to device.");
                        }
                        try {
                            deviceStatus = mapper.readValue(responseData, DeviceStatus.class);
                        } catch (JsonProcessingException e) {
                            authenticationService.logout(authenticationProperties, jsessionId);
                            throw new SdwanResponseException(e.getMessage());
                        }
                        if(deviceStatus != null && deviceStatus.getData() != null && deviceStatus.getData().size() != 0 &&
                                deviceStatus.getData().get(0).getStatusId().contains("success")){
                            deviceAttachStatus=true;
                            break;
                        }
                        try {
                            Thread.sleep(STUTUS_CHECK_DELAY_IN_SECs * 1000);
                        } catch (InterruptedException e) {
                            authenticationService.logout(authenticationProperties, jsessionId);
                            throw new SdwanResponseException(e.getMessage());
                        }
                        retryCount++;
                    }
                    if(!deviceAttachStatus){
                        logger.error("Status of attach device did not succeed within 1 minute.");
                        authenticationService.logout(authenticationProperties, jsessionId);
                        //send delayed response to Kafka
                        externalMessagingService.sendDelayedExecutionAsyncResponse(new ExecutionAsyncResponse(requestId, ExecutionStatus.FAILED, null, outputs, Collections.emptyMap()), tenantId, rcDriverProperties.getExecutionResponseDelay());
                        return new ExecutionAcceptedResponse(requestId);
                    }
                    break;
                case LIFECYCLE_INSTALL:
                    // Get Bootstrap config file and attach it to outputs.
                    uuid = (String) executionRequest.getProperties().get(DEVICE_UUID);
                    String bootstrapConfig = sdwanDriver.execute(lifecycleName,
                            executionRequest.getDeploymentLocation(), "", HttpMethod.GET, uuid, GET_BOOTSTRAP,
                            jsessionId, xsrfToken, requestId);
                    outputs.put(BOOTSTRAP_CONFIG_DATA, bootstrapConfig);
                    break;
                case LIFECYCLE_DELETE:
                    // decommission a device
                    uuid = (String) executionRequest.getProperties().get(DEVICE_UUID);
                    if (StringUtils.hasLength(uuid)) {
                        sdwanDriver.execute(lifecycleName, executionRequest.getDeploymentLocation(), "", HttpMethod.PUT,
                                uuid, DECOMMISSION_EDGE, jsessionId, xsrfToken, requestId);
                    } else {
                        logger.info("deviceUuid is empty, couldn't perform decommission of the device");
                    }
                    break;
                default:
                    authenticationService.logout(authenticationProperties, jsessionId);
                    throw new IllegalArgumentException(String.format(
                            "Requested transition [%s] is not supported by this lifecycle driver", lifecycleName));
            }
        } catch (RestClientResponseException e) {
            logger.info("Caught REST client exception when communicating with Viptela-SDWAN",e);
            authenticationService.logout(authenticationProperties, jsessionId);
            throw new SdwanResponseException(
                    String.format("Caught REST client exception when communicating with Viptela-SDWAN: " +e.getMessage()));
        }
        authenticationService.logout(authenticationProperties, jsessionId);
        //send delayed response to Kafka
        externalMessagingService.sendDelayedExecutionAsyncResponse(new ExecutionAsyncResponse(requestId, ExecutionStatus.COMPLETE, null, outputs, Collections.emptyMap()), tenantId, rcDriverProperties.getExecutionResponseDelay());
        return new ExecutionAcceptedResponse(requestId);
    }

}
