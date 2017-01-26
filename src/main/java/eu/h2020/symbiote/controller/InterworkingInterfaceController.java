package eu.h2020.symbiote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.json.simple.JSONObject;


@CrossOrigin
@RestController
public class InterworkingInterfaceController {

    private static Log log = LogFactory.getLog(InterworkingInterfaceController.class);

    @Autowired    
    private RabbitTemplate rabbitTemplate;

    @GetMapping(value="/{platformId}")
    @ResponseBody
    public ResponseEntity<String> updatePlatformId(@PathVariable String id) throws Exception {

        JSONObject platformId = new JSONObject();
        String exchangeName = "symbIoTe.platform";
        String routingKey = exchangeName + ".platformId";

        log.info("Received message for updating the platformId to :" + id);
        platformId.put("platformId", id);

        rabbitTemplate.convertSendAndReceive(exchangeName, routingKey, platformId); 
        ResponseEntity<String> responseEntity = 
            new ResponseEntity<String>("The platformId updated successfully", HttpStatus.OK);
         
        return responseEntity;
    }

}