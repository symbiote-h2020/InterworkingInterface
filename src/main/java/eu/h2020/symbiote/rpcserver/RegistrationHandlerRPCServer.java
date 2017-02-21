package eu.h2020.symbiote.rpcserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.ArrayList;

/**
* <h1>RegistrationHandlerRPCServer for Resource Access Proxy component</h1>
* This class exposes Spring AMQP interfaces for allowing Registration Handler to
* access the external world (e.g. applications, enablers or the symbIoTe Core.)
*
* @author  Vasileios Glykantzis
* @version 1.0
* @since   2017-01-26
*/

@Service
public class RegistrationHandlerRPCServer {

    private static Log log = LogFactory.getLog(RegistrationHandlerRPCServer.class);

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired   
    private String symbIoTeCoreUrl;

    @Value("${platform.id}")    
    private String platformId;

    private final java.util.Queue<ListenableFuture<ResponseEntity<JSONObject>>> futuresQueue = 
                  new ConcurrentLinkedQueue<ListenableFuture<ResponseEntity<JSONObject>>>();

   /**
   * Spring AMQP Listener for resource registration requests. This method is invoked when Registration
   * Handler sends a resource registration request and it is responsible for forwarding the message
   * to the symbIoTe core. As soon as it receives a reply, it manually sends back the response
   * to the Registration Handler via the appropriate message queue by the use of the RestAPICallback.
   * 
   * @param jsonObject A jsonObject containing the resource description
   * @param headers The AMQP headers
   */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-InterworkingInterface-registrationHandler-register_resources", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
        key = "symbIoTe.InterworkingInterface.registrationHandler.register_resources")
    )
    public void resourceRegistration(JSONObject jsonObject, @Headers() Map<String, String> headers) {

        String message = "register_resources";
        String url = symbIoTeCoreUrl + "/platforms/" + platformId + "/resources";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), httpHeaders);        

        log.info("Received " + message + " message: "+ jsonObject);
        // The AsyncRestTemplate method should change according to the request
        // Change url
        ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.exchange(
            url, HttpMethod.POST, entity, JSONObject.class);

        RestAPICallback<ResponseEntity<JSONObject>> callback = 
            new RestAPICallback<ResponseEntity<JSONObject>> (message, headers, futuresQueue, future, rabbitTemplate);
        future.addCallback(callback);
        
        futuresQueue.add(future);

    }

   /**
   * Spring AMQP Listener for resource unregistration requests. This method is invoked when Registration
   * Handler sends a resource registration request and it is responsible for forwarding the message
   * to the symbIoTe core. As soon as it receives a reply, it manually sends back the response
   * to the Registration Handler via the appropriate message queue by the use of the RestAPICallback.
   * 
   * @param id The id of the resource to be deleted
   * @param headers The AMQP headers
   */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-InterworkingInterface-registrationHandler-unregister_resources", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
        key = "symbIoTe.InterworkingInterface.registrationHandler.unregister_resources")
    )
    public void resourceUnregistration(String id, @Headers() Map<String, String> headers) {
  
        String message = "unregister_resources";
        String url = symbIoTeCoreUrl + "/platforms/" + platformId + "/resources/" + id;
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders); 

        log.info("Received " + message + " message: "+ id);

        // The AsyncRestTemplate method should change according to the request
        // Change url
        ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.exchange(
            url, HttpMethod.DELETE, entity, JSONObject.class);

        RestAPICallback<ResponseEntity<JSONObject>> callback = 
            new RestAPICallback<ResponseEntity<JSONObject>> (message, headers, futuresQueue, future, rabbitTemplate);
        future.addCallback(callback);
        
        futuresQueue.add(future);

    }

   /**
   * Spring AMQP Listener for resource update requests. This method is invoked when Registration
   * Handler sends a resource registration request and it is responsible for forwarding the message
   * to the symbIoTe core. As soon as it receives a reply, it manually sends back the response
   * to the Registration Handler via the appropriate message queue by the use of the RestAPICallback.
   * 
   * @param jsonObject A jsonObject containing the resource description
   * @param headers The AMQP headers
   */
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-InterworkingInterface-registrationHandler-update_resources", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
        key = "symbIoTe.InterworkingInterface.registrationHandler.update_resources")
    )
    public void resourceUpdate(JSONObject jsonObject, @Headers() Map<String, String> headers) {
  
        String message = "update_resources";
        Integer id = Integer.parseInt(jsonObject.get("id").toString());
        String url = symbIoTeCoreUrl + "/platforms/" + platformId + "/resources/" + id.toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), httpHeaders); 

        log.info("Received " + message + " message: "+ jsonObject);

        // The AsyncRestTemplate method should change according to the request
        // Change url
        ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.exchange(
            url, HttpMethod.PUT, entity, JSONObject.class);

        RestAPICallback<ResponseEntity<JSONObject>> callback = 
            new RestAPICallback<ResponseEntity<JSONObject>> (message, headers, futuresQueue, future, rabbitTemplate);
        future.addCallback(callback);
        
        futuresQueue.add(future);

    }

}

