package swm.thlee.linked_paper_api_server.client;

import java.util.Map;
import lombok.Data;

@Data
public class SemanticApiResponse {
  private String paperId;

  private Integer citationCount;
  private Integer referenceCount;
  private Map<String, String> externalIds;
}
