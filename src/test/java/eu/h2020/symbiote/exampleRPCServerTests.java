package eu.h2020.symbiote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Test;
import org.junit.Before;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate.RabbitConverterFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.json.simple.JSONObject;

import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest({"eureka.client.enabled=false", "spring.cloud.sleuth.enabled=false"})
public class exampleRPCServerTests {


	private static final Logger log = LoggerFactory
						.getLogger(exampleRPCServerTests.class);

    @Autowired    
    private AsyncRabbitTemplate asyncRabbitTemplate;

    @Autowired
    AsyncRestTemplate asyncRestTemplate;

    private MockRestServiceServer mockServer;

	// Execute the Setup method before the test.
	@Before
	public void setUp() throws Exception {
    
        mockServer = MockRestServiceServer.createServer(asyncRestTemplate);

	}

	@Test
	// @DisplayName("TestingexampleRPCServer's exampleInterface")
	public void exampleInterfaceTest() throws Exception {

        JSONObject query = new JSONObject();
        final AtomicReference<JSONObject> resultRef = new AtomicReference<JSONObject>();
        String example = "example";

        mockServer.expect(requestTo("http://www.example.com")).andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(2)); // Delay
                    } catch (InterruptedException ignored) {}
                    log.info("Server woke up!!!!!!");
                    return withStatus(HttpStatus.OK).body("{ \"name\" : \"" + example.toUpperCase() + "\"}").contentType(MediaType.APPLICATION_JSON).createResponse(request);
                });
            // .andRespond(withSuccess("{ \"name\" : \"" + example.toUpperCase() + "\"}", MediaType.APPLICATION_JSON));

        query.put("name", example);

        String exchangeName = "symbIoTe.InterworkingInterface";
        String routingKey = "symbIoTe.InterworkingInterface.component.example";

        log.info("Before publishing the message to the queue");

        RabbitConverterFuture<JSONObject> future = asyncRabbitTemplate.convertSendAndReceive(exchangeName, routingKey, query);

        log.info("After publishing the message to the queue");

        future.addCallback(new ListenableFutureCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject result) {

                log.info("Successful response: " + result);
                resultRef.set(result);

            }

            @Override
            public void onFailure(Throwable ex) {
                fail("Did not receive any response");
            }

        });

        while(!future.isDone())
            TimeUnit.SECONDS.sleep(1);

        JSONObject response = resultRef.get();
        assertEquals(example.toUpperCase(), response.get("name"));
	}

}
