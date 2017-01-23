package eu.h2020.symbiote.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.concurrent.ListenableFutureCallback;

import org.json.simple.JSONObject;

import org.springframework.web.context.request.async.DeferredResult;


public class RabbitMQCallback<T> implements ListenableFutureCallback<T> {

    private static Log log = LogFactory.getLog(RabbitMQCallback.class);

    private DeferredResult deferredResult;

    private String request;


    public RabbitMQCallback(String request, DeferredResult deferredResult) {
        this.request = request;
        this.deferredResult = deferredResult;    
    }


    @Override
    public void onSuccess(T result) {

        log.info(request + ": Successfully received response = " + result);
        ResponseEntity<T> responseEntity = 
                    new ResponseEntity<>(result, HttpStatus.OK);
        deferredResult.setResult(responseEntity);

    }


    @Override
    public void onFailure(Throwable ex) {
        log.info(request + ": Failed to receive response");
        ResponseEntity<Void> responseEntity = 
                    new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        deferredResult.setResult(responseEntity);
        }
}