package swm.thlee.linked_paper_api_server.config;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import swm.thlee.linked_paper_api_server.utils.CustomTracesSamplerCallback;

@Configuration
public class SentryConfig {

  @Value("${sentry.dsn}")
  private String dsn;

  @Autowired private CustomTracesSamplerCallback customTracesSamplerCallback;

  // Bean 초기화가 완료된 후 Sentry를 초기화
  @PostConstruct
  public void init() {
    Sentry.init(
        options -> {
          options.setDsn(dsn); // DSN 값 설정
          options.setEnvironment("production");
          options.setRelease("1.0.0");
          options.setTracesSampleRate(1.0); // Ensure this is set for transaction tracing
          options.setTracesSampler(customTracesSamplerCallback);
        });
  }
}
