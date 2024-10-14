package swm.thlee.linked_paper_api_server.utils;

import io.sentry.SamplingContext;
import io.sentry.SentryOptions.TracesSamplerCallback;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomTracesSamplerCallback implements TracesSamplerCallback {

  @Override
  public Double sample(SamplingContext context) {
    HttpServletRequest request =
        (HttpServletRequest) context.getCustomSamplingContext().get("request");
    if (request != null) {
      String url = request.getRequestURI();
      if ("/".equals(url)) {
        // These are important - take a big sample (50%)
        return 0d;
      }
    }
    return 1.0;
  }
}
