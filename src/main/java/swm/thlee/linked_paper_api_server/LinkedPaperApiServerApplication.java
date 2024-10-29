package swm.thlee.linked_paper_api_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
      "swm.thlee.linked_paper_api_server", // Base package for the application
      "swm.thlee.linked_paper_api_server.config" // Package where your config classes are located
    })
public class LinkedPaperApiServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(LinkedPaperApiServerApplication.class, args);
  }
}
