package me.cocoblue.chzzkeventtodiscord;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class ChzzkEventToDiscordApplication extends SpringBootServletInitializer  {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ChzzkEventToDiscordApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ChzzkEventToDiscordApplication.class, args);
    }

}
