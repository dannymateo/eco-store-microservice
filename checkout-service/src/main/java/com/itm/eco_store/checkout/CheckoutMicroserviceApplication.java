package com.itm.eco_store.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CheckoutMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckoutMicroserviceApplication.class, args);
    }
}
