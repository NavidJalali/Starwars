package com.mobimeo.challenge.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class HttpClientConfiguration {
    @Bean
    fun httpClient(): HttpClient {
        return HttpClient
            .create(
                ConnectionProvider
                    .builder("http-client")
                    .maxConnections(128)
                    .maxIdleTime(Duration.ofSeconds(5))
                    .build()
            )
    }
}