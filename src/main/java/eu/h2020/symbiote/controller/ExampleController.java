package eu.h2020.symbiote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate.RabbitConverterFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import org.json.simple.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.web.context.request.async.DeferredResult;


@CrossOrigin
@RestController
@RequestMapping("/example")
public class ExampleController {

    private static Log log = LogFactory.getLog(ExampleController.class);

    @Autowired    
    private AsyncRabbitTemplate asyncRabbitTemplate;

    @GetMapping(value="/example/{value}")
    @ResponseBody
    public DeferredResult<ResponseEntity<?>> getExample(@PathVariable String value) throws Exception {

        JSONObject query = new JSONObject();
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        query.put("value", value);

        String exchangeName = "symbIoTe.exampleComponent";
        String routingKey = "symbIoTe.exampleComponent.getexample";

        RabbitConverterFuture<JSONObject> future = asyncRabbitTemplate.convertSendAndReceive(exchangeName, routingKey, query);

        log.info("After publishing the message to the queue");

        future.addCallback(new ListenableFutureCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject result) {

                log.info("Successfully received response: " + result);
                ResponseEntity<JSONObject> responseEntity = 
                            new ResponseEntity<>(result, HttpStatus.OK);
                deferredResult.setResult(responseEntity);

            }

            @Override
            public void onFailure(Throwable ex) {
                log.info("Failed to receive response");
                ResponseEntity<Void> responseEntity = 
                            new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
                deferredResult.setResult(responseEntity);
            }

        });
        
      
        return deferredResult;
    }
}