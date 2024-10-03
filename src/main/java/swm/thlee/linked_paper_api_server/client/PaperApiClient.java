package swm.thlee.linked_paper_api_server.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import swm.thlee.linked_paper_api_server.dto.SearchPaperResult;
import swm.thlee.linked_paper_api_server.model.Paper;

@Component
public class PaperApiClient {

  private final WebClient webClient;

  // 생성자에서 외부 API URL을 받아오고 WebClient를 설정
  public PaperApiClient(
      WebClient.Builder webClientBuilder,
      @Value("${search-service-api.base-url}") String baseUrl,
      @Value("${spring.webclient.connect-timeout}") int connectTimeoutMillis,
      @Value("${spring.webclient.read-timeout}") int readTimeoutMillis) {
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

  @Cacheable(
      value = "searchResults",
      key =
          "#query + (T(org.springframework.util.CollectionUtils).isEmpty(#filterCategories) ? '' : T(String).join(',', #filterCategories)) + (#filterStartDate == null ? '' : #filterStartDate) + (#filterEndDate == null ? '' : #filterEndDate)")
  public SearchPaperResult searchPapers(
      String query, List<String> filterCategories, String filterStartDate, String filterEndDate) {

    // WebClient를 사용하여 외부 API 호출
    ApiResponse[] apiResponses =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/search")
                        .queryParam("query", query)
                        .queryParamIfPresent(
                            "filter_categories",
                            Optional.ofNullable(filterCategories)
                                .filter(f -> !f.isEmpty())
                                .map(f -> String.join(",", f)))
                        .queryParamIfPresent(
                            "filter_start_date", Optional.ofNullable(filterStartDate))
                        .queryParamIfPresent("filter_end_date", Optional.ofNullable(filterEndDate))
                        .build())
            .retrieve()
            .bodyToMono(ApiResponse[].class)
            .block(); // 비동기식 호출을 동기식으로 변환

    // API 응답을 SearchPaperResult로 매핑
    return mapToSearchPaperResult(apiResponses);
  }

  // ApiResponse 배열을 SearchPaperResult로 변환하는 메소드
  private SearchPaperResult mapToSearchPaperResult(ApiResponse[] apiResponses) {
    SearchPaperResult searchResult = new SearchPaperResult();
    searchResult.setCount(apiResponses.length);
    searchResult.setStatus("success");

    List<Paper> papers =
        List.of(apiResponses).stream().map(this::mapToPaper).collect(Collectors.toList());

    searchResult.setData(papers);
    return searchResult;
  }

  // ApiResponse를 Paper 객체로 변환하는 메소드
  private Paper mapToPaper(ApiResponse apiResponse) {
    ApiResponse.Meta meta = apiResponse.getMeta();

    String arxiv_regex = "oai:arXiv.org:(.+)"; // 'oai:arXiv.org:' 뒤에 오는 값을 추출하는 정규식
    String arxiv_oai_id = meta.getIdentifier();

    // 패턴 컴파일 및 매칭
    Pattern pattern = Pattern.compile(arxiv_regex);
    Matcher matcher = pattern.matcher(arxiv_oai_id);

    String arxiv_id;
    if (matcher.find()) {
      arxiv_id = matcher.group(1);
    } else {
      throw new IllegalArgumentException("Invalid arXiv OAI identifier format: " + arxiv_oai_id);
    }

    Paper paper = new Paper();
    paper.setId(apiResponse.getId());
    paper.setTitle(meta.getTitle());
    paper.setAbstraction(meta.getAbstractText()); // 'abstract' 필드명 변경
    paper.setJournal("arXiv.org"); // 하드코딩된 값 사용, 필요시 수정
    paper.setAuthors(List.of(meta.getAuthors().split(", "))); // authors 필드를 리스트로 변환
    paper.setCategories(List.of(meta.getCategories().split(" "))); // categories 필드를 리스트로 변환
    paper.setReference_count(0); // 응답에 reference_count가 없으므로 0으로 설정
    paper.setCitiation_count(0); // 응답에 citation_count가 없으므로 0으로 설정
    paper.setOrigin_link("https://arxiv.org/abs/" + arxiv_id); // 원본 링크 설정
    paper.setPdf_link("https://arxiv.org/pdf/" + arxiv_id + ".pdf"); // PDF 링크 설정
    paper.setDate(meta.getDatestamp());
    paper.setWeight(apiResponse.getWeight());

    return paper;
  }
}