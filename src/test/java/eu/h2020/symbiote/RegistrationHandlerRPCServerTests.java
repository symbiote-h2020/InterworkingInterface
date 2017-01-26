// package eu.h2020.symbiote;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import org.junit.Test;
// import org.junit.Before;

// import org.junit.runner.RunWith;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
// import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
// import org.springframework.amqp.rabbit.AsyncRabbitTemplate.RabbitConverterFuture;
// import org.springframework.util.concurrent.ListenableFutureCallback;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.web.client.AsyncRestTemplate;

// import java.util.concurrent.TimeUnit;
// import java.util.concurrent.atomic.AtomicReference;
// import java.util.List;
// import java.util.Arrays;
// import java.util.ArrayList;

// import org.json.simple.JSONObject;
// import org.json.simple.JSONArray;
// import org.json.simple.parser.JSONParser;

// import org.springframework.test.web.client.MockRestServiceServer;
// import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
// import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
// import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.fail;

// @RunWith(SpringJUnit4ClassRunner.class)
// @SpringBootTest({"eureka.client.enabled=false", "spring.cloud.sleuth.enabled=false"})
// public class RegistrationHandlerRPCServerTests {


// 	private static final Logger log = LoggerFactory
// 						.getLogger(RegistrationHandlerRPCServerTests.class);

//     @Autowired    
//     private AsyncRabbitTemplate asyncRabbitTemplate;

//     @Autowired    
//     AsyncRestTemplate asyncRestTemplate;

//     @Value("${symbIoTeCoreUrl}")
//     private String symbIoTeCoreUrl;

//     @Value("${platformId}")    
//     private String platformId;

//     private MockRestServiceServer mockServer;

// 	// Execute the Setup method before the test.
// 	@Before
// 	public void setUp() throws Exception {
//         mockServer = MockRestServiceServer.createServer(asyncRestTemplate);
        
// 	}

// 	@Test
// 	public void resourceRegistrationTest() throws Exception {

//         final AtomicReference<JSONObject> resultRef = new AtomicReference<JSONObject>();
//         String message = "register_resources";
//         String exchangeName = "symbIoTe.InterworkingInterface";
//         String routingKey = exchangeName + ".registrationHandler." + message;
//         String url = symbIoTeCoreUrl + "platforms/" + platformId + "/resources";
//         JSONObject location = newLocation();
//         JSONObject resource = newResource(location);
        
//         mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
//                 .andRespond(request -> {
//                     try {
//                         Thread.sleep(TimeUnit.SECONDS.toMillis(2)); // Delay
//                     } catch (InterruptedException ignored) {}

//                     JSONParser parser = new JSONParser();
//                     JSONObject response = new JSONObject();

//                     try {
//                         response = (JSONObject) parser.parse(request.getBody().toString());

//                     } catch (Exception ignored) {}

//                     response.put("id", response.get("internalId"));
//                     log.info(message + "_test: Server woke up and will answer with " + response);

//                     return withStatus(HttpStatus.OK).body(response.toString()).contentType(MediaType.APPLICATION_JSON).createResponse(request);
//                 });


//         RabbitConverterFuture<JSONObject> future = asyncRabbitTemplate.convertSendAndReceive(exchangeName, routingKey, resource);

//         future.addCallback(new ListenableFutureCallback<JSONObject>() {

//             @Override
//             public void onSuccess(JSONObject result) {

//                 log.info(message + "_test: Successful response = " + result);
//                 resultRef.set(result);

//             }

//             @Override
//             public void onFailure(Throwable ex) {
//                 fail(message + "_test: Did not receive any response");
//             }

//         });

//         while(!future.isDone())
//             TimeUnit.SECONDS.sleep(1);

//         JSONObject response = resultRef.get();
//         assertEquals(response.get("internalId"), resource.get("internalId"));
// 	}

//     @Test
//     public void resourceUnregistrationTest() throws Exception {

//         final AtomicReference<JSONObject> resultRef = new AtomicReference<JSONObject>();
//         String message = "unregister_resources";
//         String exchangeName = "symbIoTe.InterworkingInterface";
//         String routingKey = exchangeName + ".registrationHandler." + message;
//         String id = "1";
//         String url = symbIoTeCoreUrl + "platforms/" + platformId + "/resources/" + id;


//         mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.DELETE))
//                 .andRespond(request -> {
//                     try {
//                         Thread.sleep(TimeUnit.SECONDS.toMillis(2)); // Delay
//                     } catch (InterruptedException ignored) {}

//                     JSONObject location = newLocation();
//                     JSONObject resource = newResource(location);

//                     log.info(message + "_test: Server woke up!!!!!! " + resource);

//                     return withStatus(HttpStatus.OK).body(resource.toString()).contentType(MediaType.APPLICATION_JSON).createResponse(request);
//                 });


//         RabbitConverterFuture<JSONObject> future = asyncRabbitTemplate.convertSendAndReceive(exchangeName, routingKey, id);

//         future.addCallback(new ListenableFutureCallback<JSONObject>() {

//             @Override
//             public void onSuccess(JSONObject result) {

//                 log.info(message + "_test: Successful response = " + result);
//                 resultRef.set(result);

//             }

//             @Override
//             public void onFailure(Throwable ex) {
//                 fail(message + "_test: Did not receive any response");
//             }

//         });

//         while(!future.isDone())
//             TimeUnit.SECONDS.sleep(1);

//         assertEquals(resultRef.get().get("id"), 15);
//     }

//     @Test
//     public void resourceUpdateTest() throws Exception {

//         final AtomicReference<JSONObject> resultRef = new AtomicReference<JSONObject>();
//         String message = "update_resources";
//         String exchangeName = "symbIoTe.InterworkingInterface";
//         String routingKey = exchangeName + ".registrationHandler." + message;
//         JSONObject location = newLocation();
//         JSONObject resource = newResource(location);
//         Integer id = (Integer) resource.get("id");
//         String url = symbIoTeCoreUrl + "platforms/" + platformId + "/resources/" + id;

//         log.info("symbioteurl helppppppppp " + symbIoTeCoreUrl);

//         mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
//                 .andRespond(request -> {
//                     try {
//                         Thread.sleep(TimeUnit.SECONDS.toMillis(2)); // Delay
//                     } catch (InterruptedException ignored) {}

//                     JSONParser parser = new JSONParser();
//                     JSONObject response = new JSONObject();

//                     try {
//                         response = (JSONObject) parser.parse(request.getBody().toString());

//                     } catch (Exception ignored) {}

//                     response.put("id", response.get("internalId"));
//                     log.info(message + "_test: Server woke up and will answer with " + response);

//                     return withStatus(HttpStatus.OK).body(response.toString()).contentType(MediaType.APPLICATION_JSON).createResponse(request);
//                 });


//         RabbitConverterFuture<JSONObject> future = asyncRabbitTemplate.convertSendAndReceive(exchangeName, routingKey, resource);

//         future.addCallback(new ListenableFutureCallback<JSONObject>() {

//             @Override
//             public void onSuccess(JSONObject result) {

//                 log.info(message + "_test: Successful response = " + result);
//                 resultRef.set(result);

//             }

//             @Override
//             public void onFailure(Throwable ex) {
//                 fail(message + "_test: Did not receive any response");
//             }

//         });

//         while(!future.isDone())
//             TimeUnit.SECONDS.sleep(1);

//         JSONObject response = resultRef.get();
//         assertEquals(response.get("internalId"), resource.get("internalId"));
//     }


//     public JSONObject newLocation() {

//         JSONObject location = new JSONObject();

//         location.put("name", "Paris");
//         location.put("description", "A city");
//         location.put("longitude", 1.0);
//         location.put("latitude", 2.0);
//         location.put("latitude", 3.0);

//         return location;
//     }

//     public JSONObject newResource(JSONObject location) {

//         JSONObject resource = new JSONObject();
//         List<String> observedProperties = Arrays.asList("air", "temp");

//         resource.put("internalId", 123);
//         resource.put("id", 15);
//         resource.put("name", "resource1");
//         resource.put("owner", "localOwner");
//         resource.put("description", "somedesc");
//         resource.put("location", location);
//         resource.put("observedProperties", observedProperties);
//         resource.put("resourceURL", "http://aaa.bbb.ccc:5656/aaa");

//         return resource;
//     }
// }
