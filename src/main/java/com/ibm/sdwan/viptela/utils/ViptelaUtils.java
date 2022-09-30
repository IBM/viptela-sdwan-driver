package com.ibm.sdwan.viptela.utils;

import com.ibm.sdwan.viptela.config.SDWDriverProperties;
import com.ibm.sdwan.viptela.model.ExecutionRequest;
import com.ibm.sdwan.viptela.service.MissingPropertyException;
import static com.ibm.sdwan.viptela.utils.Constants.*;

import java.util.Map;

import org.springframework.util.StringUtils;

import static com.ibm.sdwan.viptela.config.SDWDriverConstants.RC_SERVER_URL;

public class ViptelaUtils {

    public static long getDelay(SDWDriverProperties sdwDriverProperties, ExecutionRequest executionRequest) {
        long vManageApiRetryDelay = sdwDriverProperties.getvManageApiDelay().toMillis();
        Object vManageApiRetryDelayObject = executionRequest.getDeploymentLocation().getProperties()
                .get("vManageApiRetryDelay");
        if (vManageApiRetryDelayObject != null) {
            if (vManageApiRetryDelayObject instanceof String) {
                vManageApiRetryDelay = Long.parseLong((String) vManageApiRetryDelayObject);
            } else {
                vManageApiRetryDelay = (Long) vManageApiRetryDelayObject;
            }
            vManageApiRetryDelay = vManageApiRetryDelay * 1000;
        }
        return vManageApiRetryDelay;
    }

    public static void validateDeploymentProperties(Map<String, Object> deploymentLocationProperties) {
        if (deploymentLocationProperties == null || deploymentLocationProperties.isEmpty()) {
            throw new MissingPropertyException("deploymentLocation property is mandatory and it is missing.");
        }
        if (deploymentLocationProperties.get(RC_SERVER_URL) instanceof String) {
            if (!StringUtils.hasLength((String) deploymentLocationProperties.get(RC_SERVER_URL))) {
                throw new MissingPropertyException(
                        " vManageHostFqdn property is mandatory and it is missing in deploymentLocation");
            }
        } else if (deploymentLocationProperties.get(RC_SERVER_URL) == null) {
            throw new MissingPropertyException(
                    " vManageHostFqdn property is mandatory and it is missing in deploymentLocation");
        }

        if (deploymentLocationProperties.get(V_MANAGE_USER) instanceof String) {
            if (!StringUtils.hasLength((String) deploymentLocationProperties.get(V_MANAGE_USER))) {
                throw new MissingPropertyException(
                        " vManageUser property is mandatory and it is missing in deploymentLocation");
            }
        } else if (deploymentLocationProperties.get(V_MANAGE_USER) == null) {
            throw new MissingPropertyException(
                    " vManageUser property is mandatory and it is missing in deploymentLocation");
        }

        if (deploymentLocationProperties.get(V_MANAGE_PASSWORD) instanceof String) {
            if (!StringUtils.hasLength((String) deploymentLocationProperties.get(V_MANAGE_PASSWORD))) {
                throw new MissingPropertyException(
                        " vManagePassword property is mandatory and it is missing in deploymentLocation");
            }
        } else if (deploymentLocationProperties.get(V_MANAGE_PASSWORD) == null) {
            throw new MissingPropertyException(
                    " vManagePassword property is mandatory and it is missing in deploymentLocation");
        }

        if (deploymentLocationProperties.get(SMART_ACCOUNT_USER) instanceof String) {
            if (!StringUtils.hasLength((String) deploymentLocationProperties.get(SMART_ACCOUNT_USER))) {
                throw new MissingPropertyException(
                        " smartAccountUser property is mandatory and it is missing in deploymentLocation");
            }
        } else if (deploymentLocationProperties.get(SMART_ACCOUNT_USER) == null) {
            throw new MissingPropertyException(
                    " smartAccountUser property is mandatory and it is missing in deploymentLocation");
        }

        if (deploymentLocationProperties.get(SMART_ACCOUNT_PASSWORD) instanceof String) {
            if (!StringUtils.hasLength((String) deploymentLocationProperties.get(SMART_ACCOUNT_PASSWORD))) {
                throw new MissingPropertyException(
                        " smartAccountPassword property is mandatory and it is missing in deploymentLocation");
            }
        } else if (deploymentLocationProperties.get(SMART_ACCOUNT_PASSWORD) == null) {
            throw new MissingPropertyException(
                    " smartAccountPassword property is mandatory and it is missing in deploymentLocation");
        }

    }
    
}
