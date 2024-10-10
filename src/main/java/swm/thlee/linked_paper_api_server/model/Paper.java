package swm.thlee.linked_paper_api_server.model;

import java.util.List;
import lombok.Data;

@Data
public class Paper {
  private String id;
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
}
