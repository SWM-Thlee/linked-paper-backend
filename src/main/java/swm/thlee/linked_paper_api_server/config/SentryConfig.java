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

  @Value("${sentry.traces-sample-rate}")
  private double traces_sample_rate;

  @Value("${sentry.environment}")
  private String env;

  @Autowired private CustomTracesSamplerCallback customTracesSamplerCallback;

  // Bean 초기화가 완료된 후 Sentry를 초기화
  @PostConstruct
  public void init() {
    Sentry.init(
        options -> {
          options.setDsn(dsn);
          options.setTracesSampleRate(traces_sample_rate);
          options.setTracesSampler(customTracesSamplerCallback);
          options.setEnvironment(env);
          options.setDebug(true);
        });
  }
}
