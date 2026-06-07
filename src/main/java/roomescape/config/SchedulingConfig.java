package roomescape.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 활성화. 테스트에서는 scheduling.enabled=false로 꺼서, 워커를 수동 호출해 결정적으로 검증한다.
 * (백그라운드 타이머가 테스트 중간에 outbox를 처리해버리면 검증이 흔들리기 때문)
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
