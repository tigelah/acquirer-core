package br.com.tigelah.acquirercore;

import br.com.tigelah.acquirercore.infrastructure.scheduling.PreAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@EnableConfigurationProperties(PreAuthProperties.class)
@SpringBootApplication
public class AcquirerCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcquirerCoreApplication.class, args);
    }

}
