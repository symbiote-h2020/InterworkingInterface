// package eu.h2020.symbiote.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.http.ResponseEntity;
// import org.springframework.http.HttpStatus;

// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;

// import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
// import org.springframework.amqp.rabbit.AsyncRabbitTemplate.RabbitConverterFuture;

// import org.json.simple.JSONObject;

// import org.springframework.web.context.request.async.DeferredResult;


// /**
// * <h1>RestController for Resource Access Proxy component</h1>
// * This class exposes REST interfaces for allowing access to the Resource
// * Access Proxy component from the external world (e.g. applications, enablers
// * or the symbIoTe Core.)
// *
// * @author  Vasileios Glykantzis
// * @version 1.0
// * @since   2017-01-26
// */
// @CrossOrigin
// @RestController
// @RequestMapping("/rap")
// public class RapRestController {

//     private static Log log = LogFactory.getLog(RapRestController.class);

//     @Autowired    
//     private AsyncRabbitTemplate asyncRabbitTemplate;

//    /**
//    * REST interface for Resource Access Proxy's readResource method. This interface
//    * is used to read a value of the resource.
//    * 
//    * @param resourceId The id of the resource to be accessed
//    * @exception Exception
//    */
//     @GetMapping(value="/resource/{resourceId}")
//     @ResponseBody
//     public DeferredResult<ResponseEntity<?>> readResource(@PathVariable String resourceId) throws Exception {

//         DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
//         JSONObject query = new JSONObject();

//         query.put("resourceId", resourceId);

//         log.info("Received request for resourceId: " + resourceId);

//         String exchangeName = "symbIoTe.rap";
//         String routingKey = "symbIoTe.rap.readResource." + resourceId;

//         RabbitConverterFuture<JSONObject> future = asyncRabbitTemplate.convertSendAndReceive(exchangeName, routingKey, query);
        
//         RabbitMQCallback<JSONObject> callback = new RabbitMQCallback<JSONObject> ("readResource", deferredResult);

//         future.addCallback(callback);
           
//         return deferredResult;
//     }

//    /**
//    * REST interface for Resource Access Proxy's readResourceHistory method. This interface
//    * is used to read historical values of the resource.
//    *
//    * @param resourceId The id of the resource to be accessed
//    * @exception Exception
//    */
//     @GetMapping(value="/resource/{resourceId}/history")
//     @ResponseBody
//     public DeferredResult<ResponseEntity<?>> readResourceHistory(@PathVariable String resourceId) throws Exception {

//         DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
//         JSONObject query = new JSONObject();

//         query.put("resourceId", resourceId);

//         log.info("Received request for resourceId: " + resourceId);

//         String exchangeName = "symbIoTe.rap";
//         String routingKey = "symbIoTe.rap.readResourceHistory." + resourceId;

//         RabbitConverterFuture<JSONObject> future = asyncRabbitTemplate.convertSendAndReceive(exchangeName, routingKey, query);
        
//         RabbitMQCallback<JSONObject> callback = new RabbitMQCallback<JSONObject> ("readResourceHistory", deferredResult);

//         future.addCallback(callback);
           
//         return deferredResult;
//     }

//    /**
//    * REST interface for Resource Access Proxy's writeResource method. This interface
//    * is used to write a value to a resource (i.e. actuator)
//    *
//    * @param resourceId The id of the resource to be accessed
//    * @param value The value to be written to the resource
//    * @exception Exception
//    */
//     @PostMapping(value="/{resourceId}")
//     @ResponseBody
//     public DeferredResult<ResponseEntity<?>> writeResource(@PathVariable String resourceId, @RequestBody String value) throws Exception {

//         DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
  
//         log.info("Received request for resourceId: " + resourceId);

//         String exchangeName = "symbIoTe.rap";
//         String routingKey = "symbIoTe.rap.writeResource." + resourceId;

//         RabbitConverterFuture<String> future = asyncRabbitTemplate.convertSendAndReceive(exchangeName, routingKey, value);
        
//         RabbitMQCallback<String> callback = new RabbitMQCallback<String> ("readResourceHistory", deferredResult);

//         future.addCallback(callback);
           
//         return deferredResult;
//     }

// }