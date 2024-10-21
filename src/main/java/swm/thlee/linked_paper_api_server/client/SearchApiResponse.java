package swm.thlee.linked_paper_api_server.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchApiResponse {
  // Getters and setters
  private String id;
  private float weight;
  private Meta meta;

  // Meta class that holds details like title, abstract, etc.
  @Data
  public static class Meta {
    // Getters and setters
    private String identifier;
    private String datestamp;
    private String title;

    @JsonProperty("abstract")
    private String abstractText;

    private String authors;
    private String categories;
  }
}
