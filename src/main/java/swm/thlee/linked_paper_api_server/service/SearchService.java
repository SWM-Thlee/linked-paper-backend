package swm.thlee.linked_paper_api_server.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swm.thlee.linked_paper_api_server.client.PaperApiClient;
import swm.thlee.linked_paper_api_server.dto.SearchPaperResult;
import swm.thlee.linked_paper_api_server.model.Paper;

@Service
public class SearchService {

  @Autowired private PaperApiClient paperApiClient;

  public SearchPaperResult searchPapers(
      String query,
      String sorting,
      int size,
      int index,
      boolean similarityLimit,
      List<String> filterCategories,
      List<String> filterJournal,
      String filterStartDate,
      String filterEndDate) {

    // 필터링 로직을 외부 API로 위임하여 처리
    SearchPaperResult externalResult =
        paperApiClient.searchPapers(query, filterCategories, filterStartDate, filterEndDate);

    // 유사성 제한, 정렬 및 인덱싱은 캐싱된 데이터에서 처리
    return processSearchResults(externalResult, sorting, size, index, similarityLimit);
  }

  public SearchPaperResult findCorrelatedPapers(
      String paperID,
      int limit,
      List<String> filterTags,
      List<String> filterCategories,
      List<String> filterJournal,
      String filterStartDate,
      String filterEndDate) {

    SearchPaperResult externalResult =
        paperApiClient.correlatedPapers(
            paperID, limit, filterCategories, filterStartDate, filterEndDate);

    return processSearchResults(externalResult, "similarity", limit, 0, false);
  }

  private SearchPaperResult processSearchResults(
      SearchPaperResult externalResult,
      String sorting,
      int size,
      int index,
      boolean similarityLimit) {
    List<Paper> papers = externalResult.getData();

    // 유사성 제한 적용
    //        if (similarityLimit) {
    //            papers = papers.stream()
    //                    .filter(paper -> paper.getWeight() > 0.1) // 유사성 스코어 0.3 이상만 포함
    //                    .collect(Collectors.toList());
    //        }

    // 정렬 적용
    if ("recency".equals(sorting)) {
      papers.sort(Comparator.comparing(Paper::getDate).reversed()); // 날짜 기준 내림차순 정렬
    } else if ("citation".equals(sorting)) {
      papers.sort(Comparator.comparing(Paper::getCitiation_count).reversed()); // 인용수 기준 내림차순 정렬
    }

    // 페이징 처리 (index와 size 기반)

    int end = Math.min(index + size, papers.size());

    if (index >= papers.size()) {
      // 인덱스 범위를 벗어나면 빈 결과 반환
      SearchPaperResult emptyResult = new SearchPaperResult();
      emptyResult.setData(Collections.emptyList());
      emptyResult.setCount(0);
      emptyResult.setStatus("No results found");
      return emptyResult;
    }

    List<Paper> paginatedPapers = papers.subList(index, end);

    // 최종 결과 생성 및 반환
    SearchPaperResult result = new SearchPaperResult();
    result.setData(paginatedPapers);
    result.setCount(paginatedPapers.size()); // 전체 결과 수
    result.setStatus("Success");

    return result;
  }
}
