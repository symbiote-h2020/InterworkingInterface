package eu.h2020.symbiote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Test;
import org.junit.Before;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.nio.charset.Charset;

import org.json.simple.JSONObject;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.core.ExchangeTypes;

import java.util.concurrent.TimeUnit;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.junit.Assert.fail;


import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.MvcResult;

// @ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest({"webEnvironment=WebEnvironment.RANDOM_PORT", "eureka.client.enabled=false", "spring.cloud.sleuth.enabled=false"})
public class RapControllerTests {


    private static final Logger log = LoggerFactory
                        .getLogger(RapControllerTests.class);

    private MediaType json = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MediaType plain = new MediaType(MediaType.TEXT_PLAIN.getType(),
            MediaType.TEXT_PLAIN.getSubtype(),
            Charset.forName("utf8"));

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    // Execute the Setup method before the test.
    @Before
    public void setUp() throws Exception {

        mockMvc = webAppContextSetup(webApplicationContext).build();


    }

    @Test
    public void testReadResource() throws Exception {

        String id = "1";

        MvcResult mvcResult = mockMvc.perform(get("/rap/resource/" + id))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mvcResult = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            // .andExpect(content().contentType(json))
            .andExpect(jsonPath("$.value", is("1")))
            .andReturn();
            
        log.info("MvcResult is: " + mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testReadResourceHistory() throws Exception {

        String id = "1";

        MvcResult mvcResult = mockMvc.perform(get("/rap/resource/" + id + "/history"))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mvcResult = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(content().contentType(json))
            .andExpect(jsonPath("$.value", is("1")))
            .andReturn();
            
        log.info("MvcResult is: " + mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testWriteResource() throws Exception {

        String id = "1";

        MvcResult mvcResult = mockMvc.perform(post("/rap/" + id).content("OK???"))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mvcResult = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(content().contentType(plain))
            // .andExpect()
            .andReturn();
            
        log.info("MvcResult is: " + mvcResult.getResponse().getContentAsString());

    }


    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-rap-readResource", durable = "false", autoDelete = "true", exclusive = "true"),
        exchange = @Exchange(value = "symbIoTe.rap", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
        key = "symbIoTe.rap.readResource.*")
    )
    public JSONObject readResourceListener(JSONObject jsonObject) {
       
        log.info("Received message: " + jsonObject);

        jsonObject.put("value", jsonObject.get("resourceId"));

        log.info("Returns message: " + jsonObject);

        return jsonObject;
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-rap-readResourceHistory", durable = "false", autoDelete = "true", exclusive = "true"),
        exchange = @Exchange(value = "symbIoTe.rap", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
        key = "symbIoTe.rap.readResourceHistory.*")
    )
    public JSONObject readResourceHistoryListener(JSONObject jsonObject) {
       
        log.info("Received message: " + jsonObject);

        jsonObject.put("value", jsonObject.get("resourceId"));

        log.info("Returns message: " + jsonObject);

        return jsonObject;
    }

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "symbIoTe-rap-writeResource", durable = "false", autoDelete = "true", exclusive = "true"),
        exchange = @Exchange(value = "symbIoTe.rap", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
        key = "symbIoTe.rap.writeResource.*")
    )
    public String writeResourceListener(String string) {
       
        log.info("Received message: " + string);

        return "YES";
    }

}