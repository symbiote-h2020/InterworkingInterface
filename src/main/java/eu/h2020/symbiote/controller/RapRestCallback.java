package eu.h2020.symbiote.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.concurrent.ListenableFutureCallback;

import org.springframework.web.context.request.async.DeferredResult;


/**
* <h1>A Callback for listening to asynchronous RabbitMQ replies</h1>
* This class extends the ListenableFutureCallback class and uses the
* DeferredResult class for replying asynchronously to the HTTP Request 
* received by a controller with the Spring AMQP reply it receives.
*
* @author  Vasileios Glykantzis
* @version 1.0
* @since   2017-01-26
*/
public class RapRestCallback<T> implements ListenableFutureCallback<T> {

    private static Log log = LogFactory.getLog(RapRestCallback.class);

    private DeferredResult deferredResult;

    private String request;

   /**
   * Constructor of the RapRestCallback
   *
   * @param request String describing the type of request. Used in logging.
   * @param deferredResult  The deferredResult which is modified to serve the HTTP request.
   */
    public RapRestCallback(String request, DeferredResult deferredResult) {
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