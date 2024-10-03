package swm.thlee.linked_paper_api_server.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swm.thlee.linked_paper_api_server.dto.SearchPaperResult;
import swm.thlee.linked_paper_api_server.service.SearchService;

@RestController
@RequestMapping("/search")
public class SearchController {

  @Autowired private SearchService searchService;

  @GetMapping
  public ResponseEntity<SearchPaperResult> findSearchResult(
      @RequestParam("query") String query,
      @RequestParam(value = "sorting", defaultValue = "similarity") String sorting,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "index", defaultValue = "0") int index,
      @RequestParam(value = "similarity_limit", defaultValue = "true") boolean similarityLimit,
      @RequestParam(value = "filter_category", required = false) List<String> filterCategories,
      @RequestParam(value = "filter_journal", required = false) List<String> filterJournal,
      @RequestParam(value = "filter_start_date", required = false) String filterStartDate,
      @RequestParam(value = "filter_end_date", required = false) String filterEndDate) {

    SearchPaperResult result =
        searchService.searchPapers(
            query,
            sorting,
            size,
            index,
            similarityLimit,
            filterCategories,
            filterJournal,
            filterStartDate,
            filterEndDate);

    return ResponseEntity.ok(result);
  }
}
