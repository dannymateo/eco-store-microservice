package com.itm.eco_store.checkout.infrastructure.config;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class NatsConfig {

    @Bean(destroyMethod = "close")
    public Connection natsConnection(NatsCheckoutProperties properties) throws Exception {
        Options options = new Options.Builder()
                .server(properties.url())
                .connectionTimeout(Duration.ofSeconds(5))
                .build();
        return Nats.connect(options);
    }
}
