package eu.h2020.symbiote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.HandlerMapping;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.async.DeferredResult;

import org.json.simple.JSONObject;



/**
* <h1>RestController for Resource Access Proxy component</h1>
* This class exposes REST interfaces for allowing access to the Resource
* Access Proxy component from the external world (e.g. applications, enablers
* or the symbIoTe Core.). Specifically, it serves as a reverse proxy for
* Resource Registration Handler.
*
* @author  Vasileios Glykantzis
* @version 1.0
* @since   2017-01-26
*/
@CrossOrigin
@RestController
@RequestMapping("/rap")
public class RapRestController {

    private static Log log = LogFactory.getLog(RapRestController.class);

    @Value("${rap.url}")
    private String rapUrl;

    @Autowired    
    AsyncRestTemplate asyncRestTemplate;

   /**
   * REST interface for forwarding all GET requests to Resource Access Proxy
   * 
   * @param request The request to be forwarded to the Resource Access Proxy
   */
    @GetMapping(value="/**")
    public DeferredResult<ResponseEntity<?>> rapGet(HttpServletRequest request) throws Exception {

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        String requestedUrl = (String) request.getAttribute(
            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String prefix = "/rap";
        String url = rapUrl + requestedUrl.substring(requestedUrl.indexOf(prefix) + prefix.length());;
                
        String message = "Received GET request for RAP";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);        

        log.info("Received a GET request for RAP: " + requestedUrl);
        log.info("Forwarding the request to: " + url);

        ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.exchange(
            url, HttpMethod.GET, entity, JSONObject.class);

        RapRestCallback<ResponseEntity<JSONObject>> callback = 
            new RapRestCallback<ResponseEntity<JSONObject>> (message, deferredResult);
        future.addCallback(callback);
           
        return deferredResult;
    }


   /**
   * REST interface for forwarding all POST requests to Resource Access Proxy
   * 
   * @param request The request to be forwarded to the Resource Access Proxy
   */
    @PostMapping(value="/**")
    public DeferredResult<ResponseEntity<?>> rapPost(@RequestBody String value, HttpServletRequest request) throws Exception {

        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        String requestedUrl = (String) request.getAttribute(
            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String prefix = "/rap";
        String url = rapUrl + requestedUrl.substring(requestedUrl.indexOf(prefix) + prefix.length());;
                
        String message = "Received POST request for RAP";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(new JSONObject().toString(), httpHeaders);        

        log.info("Received a POST request for RAP: " + requestedUrl);
        log.info("Forwarding the request to: " + url);

        ListenableFuture<ResponseEntity<JSONObject>> future = asyncRestTemplate.exchange(
            url, HttpMethod.POST, entity, JSONObject.class);

        RapRestCallback<ResponseEntity<JSONObject>> callback = 
            new RapRestCallback<ResponseEntity<JSONObject>> (message, deferredResult);
        future.addCallback(callback);
           
        return deferredResult;
    }

}