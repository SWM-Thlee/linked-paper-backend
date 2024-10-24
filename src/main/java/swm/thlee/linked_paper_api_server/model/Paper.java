package swm.thlee.linked_paper_api_server.model;

import java.util.List;
import java.util.Objects;
import lombok.Data;

@Data
public class Paper {
  private String id;
  private String arxiv_id;
  private String title;
  private String abstraction;
  private String journal;
  private List<String> authors;
  private List<String> categories;
  private int reference_count;
  private int citiation_count;
  private String origin_link;
  private String pdf_link;
  private String date;
  private float weight;

  public String getSemanticArxivId() {
    return "ARXIV:" + this.arxiv_id;
  }

  @Override
  public boolean equals(Object o) {
    // arxiv_id를 기준으로 중복 제거
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Paper paper = (Paper) o;
    return Objects.equals(arxiv_id, paper.arxiv_id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(arxiv_id);
  }
}
