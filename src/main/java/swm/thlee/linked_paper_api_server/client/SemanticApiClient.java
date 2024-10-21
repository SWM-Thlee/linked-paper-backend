package swm.thlee.linked_paper_api_server.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import swm.thlee.linked_paper_api_server.dto.SearchPaperResult;
import swm.thlee.linked_paper_api_server.model.Paper;

@Component
public class SemanticApiClient {

  private final WebClient webClient;

  public SemanticApiClient(
      WebClient.Builder webClientBuilder,
      @Value("${spring.webclient.connect-timeout}") int connectTimeoutMillis,
      @Value("${spring.webclient.read-timeout}") int readTimeoutMillis) {
    String baseUrl = "https://api.semanticscholar.org";
    // HttpClient 설정 (application.yml에서 읽은 타임아웃 값 사용)
    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis) // 설정된 연결 타임아웃 적용
            .responseTimeout(Duration.ofMillis(readTimeoutMillis)) // 설정된 읽기 타임아웃 적용
            .doOnConnected(
                conn ->
                    conn.addHandlerLast(
                        new ReadTimeoutHandler(
                            readTimeoutMillis, TimeUnit.MILLISECONDS))); // 응답 타임아웃 설정

    this.webClient =
        webClientBuilder
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient)) // HttpClient 적용
            .build();
  }

  public SearchPaperResult getExtrainfo(SearchPaperResult searchPaperResult) {
    try {
      SemanticApiResponse[] semanticApiResponse =
          webClient
              .post()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/graph/v1/paper/batch")
                          .queryParam("fields", "citationCount,referenceCount,externalIds")
                          .build())
              .body(
                  BodyInserters.fromValue(
                      Map.of(
                          "ids",
                          searchPaperResult.getData().stream()
                              .map(Paper::getSemanticArxivId)
                              .collect(Collectors.toList()))))
              .retrieve()
              .bodyToMono(SemanticApiResponse[].class)
              .block();

      return aggregateResult(searchPaperResult, semanticApiResponse);

    } catch (Exception e) {
      System.err.println("Error occurred from 3rd party api: " + e.getMessage());
      return searchPaperResult; // 예외 발생 시 기존 searchPaperResult 반환
    }
  }

  public SearchPaperResult aggregateResult(
      SearchPaperResult searchPaperResult, SemanticApiResponse[] semanticApiResponses) {
    searchPaperResult
        .getData()
        .forEach(
            paper -> {
              for (SemanticApiResponse semanticApiResponse : semanticApiResponses) {
                if (paper
                    .getArxiv_id()
                    .equals(semanticApiResponse.getExternalIds().getOrDefault("ArXiv", "none"))) {
                  paper.setCitiation_count(semanticApiResponse.getCitationCount());
                  paper.setReference_count(semanticApiResponse.getReferenceCount());
                }
              }
            });
    return searchPaperResult;
  }
}
