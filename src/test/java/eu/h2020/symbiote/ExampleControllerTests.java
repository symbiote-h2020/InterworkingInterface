// package eu.h2020.symbiote;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import org.junit.Test;
// import org.junit.Before;

// import org.junit.runner.RunWith;
// import org.springframework.context.annotation.Bean;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.test.context.ContextConfiguration;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
// import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.web.context.WebApplicationContext;
// import org.springframework.web.client.AsyncRestTemplate;
// import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

// import java.nio.charset.Charset;

// import org.json.simple.JSONObject;

// import org.springframework.amqp.rabbit.connection.ConnectionFactory;
// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.amqp.rabbit.annotation.Queue;
// import org.springframework.amqp.rabbit.annotation.Exchange;
// import org.springframework.amqp.rabbit.annotation.QueueBinding;
// import org.springframework.amqp.core.ExchangeTypes;

// import java.util.concurrent.TimeUnit;
// import org.springframework.util.concurrent.ListenableFuture;
// import org.springframework.util.concurrent.ListenableFutureCallback;
// import java.util.concurrent.atomic.AtomicReference;

// import static org.hamcrest.Matchers.is;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
// import static org.junit.Assert.fail;


// import org.springframework.test.web.servlet.ResultActions;
// import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
// import org.springframework.test.web.servlet.MvcResult;

// // @ContextConfiguration
// @RunWith(SpringJUnit4ClassRunner.class)
// @SpringBootTest({"webEnvironment=WebEnvironment.RANDOM_PORT", "eureka.client.enabled=false", "spring.cloud.sleuth.enabled=false"})
// public class ExampleControllerTests {


// 	private static final Logger log = LoggerFactory
// 						.getLogger(ExampleControllerTests.class);

//     private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
//             MediaType.APPLICATION_JSON.getSubtype(),
//             Charset.forName("utf8"));

//     @Autowired
//     private WebApplicationContext webApplicationContext;

//     @Autowired
//     AsyncRestTemplate asyncRestTemplate;

//     private MockMvc mockMvc;
//     // private SimpleMessageListenerContainer container;

//     final static String queueName = "symbIoTe-exampleComponent-getexample";

// 	// Execute the Setup method before the test.
// 	@Before
// 	public void setUp() throws Exception {

// 		mockMvc = webAppContextSetup(webApplicationContext).build();


// 	}

// 	@Test
// 	// @DisplayName("Testing Access Controller's GET method")
// 	public void testGet() throws Exception {

//         String value = "getexample";

// 		MvcResult mvcResult = mockMvc.perform(get("/example/example/" + value))
// 			.andExpect(status().isOk())
// 			.andExpect(request().asyncStarted())
// 			.andReturn();

// 		mvcResult = mockMvc.perform(asyncDispatch(mvcResult))
// 			.andExpect(status().isOk())
// 			.andExpect(content().contentType(contentType))
//             .andExpect(jsonPath("$.value", is(value.toUpperCase())))
//             .andReturn();
            
//         log.info("MvcResult is: " + mvcResult.getResponse().getContentAsString());

// 	}

//     @RabbitListener(bindings = @QueueBinding(
//         value = @Queue(value = "symbIoTe-exampleComponent-getexample", durable = "false", autoDelete = "true", exclusive = "true"),
//         exchange = @Exchange(value = "symbIoTe.exampleComponent", ignoreDeclarationExceptions = "true", type = ExchangeTypes.DIRECT),
//         key = "symbIoTe.exampleComponent.getexample")
//     )
//     public JSONObject getExampleListener(JSONObject jsonObject) {
    
//         log.info("Received message: " + jsonObject);

//         String value = jsonObject.get("value").toString();
//         jsonObject.put("value", value.toUpperCase());

//         return jsonObject;
//     }

// }
