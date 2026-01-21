package br.com.tigelah.acquirercore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class AcquirerCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcquirerCoreApplication.class, args);
    }

}
