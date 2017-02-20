package eu.h2020.symbiote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;

import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

/**
 * Created by mateuszl on 22.09.2016.
 */
@EnableAsync
@EnableDiscoveryClient
@SpringBootApplication
public class InterworkingInterfaceApplication extends AsyncConfigurerSupport {

	private static Log log = LogFactory.getLog(InterworkingInterfaceApplication.class);

    @Value("${symbIoTe.core.url}")
    private String symbIoTeCoreUrl; 

    @Value("${rap.url}")
    private String rapUrl; 

	public static void main(String[] args) {
		SpringApplication.run(InterworkingInterfaceApplication.class, args);

    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("InterworkingInterfaceApplication-");
        executor.initialize();
        return executor;
    }

    // @Bean
    // public AlwaysSampler defaultSampler() {
    //     return new AlwaysSampler();
    // }

    @Bean(name="symbIoTeCoreUrl")
    String symbIoTeCoreUrl() {
        return symbIoTeCoreUrl.replaceAll("(/*)$", "");
    }

    @Bean(name="rapUrl")
    String rapUrl() {
        return rapUrl.replaceAll("(/*)$", "");
    }

    @Bean
    AsyncRestTemplate asyncRestTemplate() {
        return new AsyncRestTemplate();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        return factory;
    }

    @Bean Jackson2JsonMessageConverter jackson2JsonMessageConverter() {

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        /**
         * It is necessary to register the GeoJsonModule, otherwise the GeoJsonPoint cannot
         * be deserialized by Jackson2JsonMessageConverter.
         */
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.registerModule(new GeoJsonModule());
        // converter.setJsonObjectMapper(mapper);
        return converter;
    }


    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        // connectionFactory.setPublisherConfirms(true);
        // connectionFactory.setPublisherReturns(true);
        // connectionFactory.setUsername("guest");
        // connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public AsyncRabbitTemplate asyncRabbitTemplate(RabbitTemplate rabbitTemplate) {

       /**
         * The following AsyncRabbitTemplate constructor uses "Direct replyTo" for replies.
         */
        AsyncRabbitTemplate asyncRabbitTemplate = new AsyncRabbitTemplate(rabbitTemplate);
        asyncRabbitTemplate.setReceiveTimeout(5000);

        return asyncRabbitTemplate;
    }

}
