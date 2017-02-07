package eu.h2020.symbiote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Test;
import org.junit.Before;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.nio.charset.Charset;

import org.json.simple.JSONObject;

import java.util.concurrent.TimeUnit;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.test.web.client.MockRestServiceServer;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
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

    @Autowired    
    AsyncRestTemplate asyncRestTemplate;

    @Value("${rap.url}")
    private String rapUrl;

    private MockMvc mockMvc;

    private MockRestServiceServer mockServer;

    // Execute the Setup method before the test.
    @Before
    public void setUp() throws Exception {

        mockMvc = webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(asyncRestTemplate);


    }

    @Test
    public void testRapGet() throws Exception {

        String value = "1";
        String url = rapUrl + "/testRapGet";

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(2)); // Delay
                    } catch (InterruptedException ignored) {}

                    JSONObject response = new JSONObject();
                    response.put("value", value);
                    log.info("testRapGet: Server woke up and will answer with " + response);

                    return withStatus(HttpStatus.OK).body(response.toString()).contentType(MediaType.APPLICATION_JSON).createResponse(request);
                });

        MvcResult mvcResult = mockMvc.perform(get("/rap/testRapGet"))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mvcResult = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(content().contentType(json))
            .andExpect(jsonPath("$.body.value", is(value)))
            .andReturn();
            
        log.info("MvcResult is: " + mvcResult.getResponse().getContentAsString());

    }


    @Test
    public void testRapPost() throws Exception {

        String value = "1";
        String url = rapUrl + "/testRapPost";

        mockServer.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(2)); // Delay
                    } catch (InterruptedException ignored) {}

                    JSONObject response = new JSONObject();
                    response.put("value", value);
                    log.info("testRapPost: Server woke up and will answer with " + response);

                    return withStatus(HttpStatus.OK).body(response.toString()).contentType(MediaType.APPLICATION_JSON).createResponse(request);
                });

        MvcResult mvcResult = mockMvc.perform(post("/rap/testRapPost").content("OK???"))
            .andExpect(status().isOk())
            .andExpect(request().asyncStarted())
            .andReturn();

        mvcResult = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk())
            .andExpect(content().contentType(json))
            // .andExpect()
            .andReturn();
            
        log.info("MvcResult is: " + mvcResult.getResponse().getContentAsString());

    }


}