package eu.h2020.symbiote.rpcserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.http.ResponseEntity;

import org.springframework.web.client.AsyncRestTemplate;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import org.json.simple.JSONObject;

import java.util.Map;
import java.util.Queue;

public class RestAPICallback<T> implements ListenableFutureCallback<T> {

    private static Log log = LogFactory.getLog(RestAPICallback.class);

    private String request;
    private Map<String, String> headers;
    private ListenableFuture<ResponseEntity<JSONObject>> future;
    private RabbitTemplate rabbitTemplate;
    private Queue<ListenableFuture<ResponseEntity<JSONObject>>> futuresQueue;


    public RestAPICallback(String request, Map<String, String> headers, Queue<ListenableFuture<ResponseEntity<JSONObject>>> futuresQueue,
            ListenableFuture<ResponseEntity<JSONObject>> future, RabbitTemplate rabbitTemplate) {
        this.request = request;
        this.headers = headers;    
        this.future = future;
        this.rabbitTemplate = rabbitTemplate;    
    }


    @Override
    public void onSuccess(T result) {
        ResponseEntity responseEntity = (ResponseEntity) result;
        log.info(request + ": Successfully received response from server: " + result);

        rabbitTemplate.convertAndSend(headers.get("amqp_replyTo"), responseEntity.getBody(),
            m -> {
                    m.getMessageProperties().setCorrelationIdString(headers.get("amqp_correlationId"));
                    return m;
                });
        futuresQueue.remove(future);
    }
 

    @Override
    public void onFailure(Throwable t) {
        log.info(request + ": Failed to fetch result from remote service", t);
        JSONObject newObject = new JSONObject();
        newObject.put("exception", t);

        rabbitTemplate.convertAndSend(headers.get("amqp_replyTo"), newObject,
            m -> {
                    m.getMessageProperties().setCorrelationIdString(headers.get("amqp_correlationId"));
                    return m;
                });

        futuresQueue.remove(future);
    }
}

