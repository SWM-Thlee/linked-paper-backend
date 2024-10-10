package swm.thlee.linked_paper_api_server.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
  // 필요 시 캐시 프로바이더 설정 (예: Redis)
}
