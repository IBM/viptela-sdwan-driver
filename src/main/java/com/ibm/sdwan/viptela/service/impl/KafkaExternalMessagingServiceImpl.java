package com.ibm.sdwan.viptela.service.impl;

import java.time.Duration;

import com.ibm.sdwan.viptela.config.SDWDriverProperties;
import com.ibm.sdwan.viptela.driver.SdwanResponseException;
import com.ibm.sdwan.viptela.model.alm.ExecutionAsyncResponse;
import com.ibm.sdwan.viptela.service.ExternalMessagingService;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.ibm.sdwan.viptela.utils.Constants.*;
@Service
public class KafkaExternalMessagingServiceImpl implements ExternalMessagingService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaExternalMessagingServiceImpl.class);

    private final SDWDriverProperties properties;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaExternalMessagingServiceImpl(SDWDriverProperties properties, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override public void sendExecutionAsyncResponse(ExecutionAsyncResponse request,String tenantId) {
        try {
            final String message = objectMapper.writeValueAsString(request);
            if(tenantId.equals("1")) {
                ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(properties.getTopics().getLifecycleResponsesTopic(), message);
                future.addCallback(sendResult -> logger.debug("ExecutionAsyncResponse successfully sent"),
                        exception -> logger.warn("Exception while sending ExecutionAsyncResponse", exception));
            }else {
                logger.info("tenantId in Kafka: " + tenantId);
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(properties.getTopics().getLifecycleResponsesTopic(), message);
                producerRecord.headers().add(TENANT_ID, tenantId.getBytes());
                ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(producerRecord);
                future.addCallback(sendResult -> logger.debug("ExecutionAsyncResponse successfully sent"),
                        exception -> logger.warn("Exception while sending ExecutionAsyncResponse", exception));
            }
        } catch (JsonProcessingException e) {
            logger.error("Exception generating message text from ExecutionAsyncResponse", e);
            throw new SdwanResponseException("Internal Error in generating message text from ExecutionAsyncResponse");
        }
    }

    @Override
    @Async
    public void sendDelayedExecutionAsyncResponse(ExecutionAsyncResponse request, String tenantId, Duration delay) {
        if (delay != null) {
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                logger.error("Thread interrupted during sleep", e);
                throw new SdwanResponseException("Internal Error in Thread sleep");
            }
        }
        sendExecutionAsyncResponse(request,tenantId);
    }

}