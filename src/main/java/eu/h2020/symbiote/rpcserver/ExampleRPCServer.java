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

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import org.json.simple.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;


@Service
public class ExampleRPCServer {

    private static Log log = LogFactory.getLog(ExampleRPCServer.class);

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final java.util.Queue<ListenableFuture<ResponseEntity<JSONObject>>> futuresQueue = 
                  new ConcurrentLinkedQueue<ListenableFuture<ResponseEntity<JSONObject>>>();

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-InterworkingInterface-component-example", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
        key = "symbIoTe.InterworkingInterface.component.example")
    )
    public void exampleInterface(JSONObject jsonObject, @Headers() Map<String, String> headers) {
  
        log.info("Received message: " + jsonObject);
        log.info(headers);

        // The AsyncRestTemplate method should change according to the request
        ListenableFuture<ResponseEntity<JSONObject>> exampleQuery = asyncRestTemplate.getForEntity("http://www.example.com", JSONObject.class);

        exampleQuery.addCallback(
                new ListenableFutureCallback<ResponseEntity<JSONObject>>() {

                    private Map<String, String> headers;
                    private ListenableFuture<ResponseEntity<JSONObject>> future;
                    private RabbitTemplate rabbitTemplate;

                    @Override
                    public void onSuccess(ResponseEntity<JSONObject> result) {
                        log.info("Successfully received response from server: " + result);
                        rabbitTemplate.convertAndSend(headers.get("amqp_replyTo"), result.getBody(),
                            m -> {
                                    m.getMessageProperties().setCorrelationIdString(headers.get("amqp_correlationId"));
                                    return m;
                                 });
                    }
 
                    @Override
                    public void onFailure(Throwable t) {
                        log.info("Failed to fetch result from remote service", t);
                        JSONObject newObject = new JSONObject();
                        newObject.put("exception", t);

                        rabbitTemplate.convertAndSend(headers.get("amqp_replyTo"), newObject,
                            m -> {
                                    m.getMessageProperties().setCorrelationIdString(headers.get("amqp_correlationId"));
                                    return m;
                                 });
                    }

                    private ListenableFutureCallback<ResponseEntity<JSONObject>> init(Map<String, String> messageHeaders, 
                             ListenableFuture<ResponseEntity<JSONObject>> futureObject, RabbitTemplate template) {
                        headers = messageHeaders;
                        future = futureObject;
                        rabbitTemplate = template;
                        return this;
                    }
                }.init(headers, exampleQuery, rabbitTemplate) 
        );
        
        futuresQueue.add(exampleQuery);

    }
}

