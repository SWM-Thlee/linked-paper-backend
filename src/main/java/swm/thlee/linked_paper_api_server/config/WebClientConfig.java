package swm.thlee.linked_paper_api_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    return builder.build();
  }
}
