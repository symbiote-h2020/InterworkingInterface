package eu.h2020.symbiote.rpcserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Headers;

import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import org.json.simple.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;


@Service
public class RegistrationHandlerRPCServer {

    private static Log log = LogFactory.getLog(RegistrationHandlerRPCServer.class);

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final java.util.Queue<ListenableFuture<ResponseEntity<JSONObject>>> futuresQueue = 
                  new ConcurrentLinkedQueue<ListenableFuture<ResponseEntity<JSONObject>>>();

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-InterworkingInterface-registrationHandler-register_resources", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
        key = "symbIoTe.InterworkingInterface.registrationHandler.register_resources")
    )
    public void resourceRegistration(JSONObject jsonObject, @Headers() Map<String, String> headers) {

        String message = "register_resources";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), httpHeaders);        

        log.info("Received " + message + " message: "+ jsonObject);

        // The AsyncRestTemplate method should change according to the request
        // Change url
        ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.exchange(
            "http://www.example.com/" + message, HttpMethod.POST, entity, JSONObject.class);

        RestAPICallback<ResponseEntity<JSONObject>> callback = 
            new RestAPICallback<ResponseEntity<JSONObject>> (message, headers, futuresQueue, future, rabbitTemplate);
        future.addCallback(callback);
        
        futuresQueue.add(future);

    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-InterworkingInterface-registrationHandler-unregister_resources", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
        key = "symbIoTe.InterworkingInterface.registrationHandler.unregister_resources")
    )
    public void resourceUnregistration(JSONObject jsonObject, @Headers() Map<String, String> headers) {
  
        String message = "unregister_resources";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), httpHeaders); 

        log.info("Received " + message + " message: "+ jsonObject);

        // The AsyncRestTemplate method should change according to the request
        // Change url
        ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.exchange(
            "http://www.example.com/" + message, HttpMethod.DELETE, entity, JSONObject.class);

        RestAPICallback<ResponseEntity<JSONObject>> callback = 
            new RestAPICallback<ResponseEntity<JSONObject>> (message, headers, futuresQueue, future, rabbitTemplate);
        future.addCallback(callback);
        
        futuresQueue.add(future);

    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-InterworkingInterface-registrationHandler-update_resources", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
        key = "symbIoTe.InterworkingInterface.registrationHandler.update_resources")
    )
    public void resourceUpdate(JSONObject jsonObject, @Headers() Map<String, String> headers) {
  
        String message = "update_resources";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), httpHeaders); 

        log.info("Received " + message + " message: "+ jsonObject);

        // The AsyncRestTemplate method should change according to the request
        // Change url
        // ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.getForEntity("http://www.example.com/" + message, JSONObject.class);
        ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.exchange(
            "http://www.example.com/" + message, HttpMethod.PUT, entity, JSONObject.class);

        RestAPICallback<ResponseEntity<JSONObject>> callback = 
            new RestAPICallback<ResponseEntity<JSONObject>> (message, headers, futuresQueue, future, rabbitTemplate);
        future.addCallback(callback);
        
        futuresQueue.add(future);

    }

}

