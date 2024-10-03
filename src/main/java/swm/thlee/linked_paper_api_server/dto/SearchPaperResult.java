package swm.thlee.linked_paper_api_server.dto;

import java.util.List;
import swm.thlee.linked_paper_api_server.model.Paper;

public class SearchPaperResult {
  private int count;
  private String status;
  private List<Paper> data;

  // Getters and Setters
  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<Paper> getData() {
    return data;
  }

  public void setData(List<Paper> data) {
    this.data = data;
  }
}
