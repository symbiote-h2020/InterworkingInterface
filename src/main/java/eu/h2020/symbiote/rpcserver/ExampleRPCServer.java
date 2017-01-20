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

import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import org.json.simple.JSONObject;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class ExampleRPCServer {

    private static Log log = LogFactory.getLog(ExampleRPCServer.class);

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;


    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-InterworkingInterface-component-example", durable = "true", autoDelete = "false", exclusive = "false"),
        exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
        key = "symbIoTe.InterworkingInterface.component.example")
    )
    public JSONObject exampleInterface(JSONObject jsonObject) {

        AtomicReference<ResponseEntity<JSONObject>> resultRef = new AtomicReference<ResponseEntity<JSONObject>>();
        
        log.info("Received message: " + jsonObject);

        // The AsyncRestTemplate method should change according to the request
        ListenableFuture<ResponseEntity<JSONObject>> exampleQuery = asyncRestTemplate.getForEntity("http://www.example.com", JSONObject.class);
        exampleQuery.addCallback(
                new ListenableFutureCallback<ResponseEntity<JSONObject>>() {
                    @Override
                    public void onSuccess(ResponseEntity<JSONObject> result) {
                        log.info("Successfully received response from server: " + result);
                        resultRef.set(result);
                    }
 
                    @Override
                    public void onFailure(Throwable t) {
                        log.info("Failed to fetch result from remote service", t);
                        ResponseEntity<JSONObject> responseEntity = 
                            new ResponseEntity<JSONObject>(new JSONObject(), HttpStatus.SERVICE_UNAVAILABLE);
                        resultRef.set(responseEntity);
                    }
                }
        );
        return resultRef.get().getBody();
    }
}

    // @RabbitListener(bindings = @QueueBinding(
    //     value = @Queue(value = "symbIoTe-exampleComponent-getexample", durable = "false", autoDelete = "true", exclusive = "true"),
    //     exchange = @Exchange(value = "symbIoTe.exampleComponent", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
    //     key = "symbIoTe.exampleComponent.getexample")
    // )
    // public JSONObject getExampleListener(JSONObject jsonObject) throws Exception {
    
    //     log.info("Received message: " + jsonObject);

    //     String value = jsonObject.get("value").toString();
    //     jsonObject.put("value", value.toUpperCase());

    //     TimeUnit.SECONDS.sleep(1);
    //     return jsonObject;
    // }

    // @RabbitListener(bindings = @QueueBinding(
    //     value = @Queue(value = "symbIoTe-InterworkingInterface-component-example", durable = "true", autoDelete = "false", exclusive = "false"),
    //     exchange = @Exchange(value = "symbIoTe.InterworkingInterface", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
    //     key = "symbIoTe.InterworkingInterface.component.example")
    // )
    // public DeferredResult<JSONObject> exampleInterface(JSONObject jsonObject) {

    //     DeferredResult<JSONObject> deferredResult = new DeferredResult<>();
        
    //     log.info("Received message: " + jsonObject);

    //     // The AsyncRestTemplate method should change according to the request
    //     ListenableFuture<ResponseEntity<JSONObject>> exampleQuery = asyncRestTemplate.getForEntity("http://localhost:8101/example/example/bill", JSONObject.class);
    //     exampleQuery.addCallback(
    //             new ListenableFutureCallback<ResponseEntity<JSONObject>>() {
    //                 @Override
    //                 public void onSuccess(ResponseEntity<JSONObject> result) {
    //                     log.info("Successfully received response from server: " + result);
    //                     deferredResult.setResult(result.getBody());
    //                 }
 
    //                 @Override
    //                 public void onFailure(Throwable t) {
    //                     log.info("Failed to fetch result from remote service", t);
    //                     deferredResult.setResult(new JSONObject());
    //                 }
    //             }
    //     );
    //     return deferredResult;
    // }