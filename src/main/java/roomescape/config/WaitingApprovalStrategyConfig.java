package roomescape.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "waiting", name = "approval-strategy", havingValue = "manual")
public class WaitingApprovalStrategyConfig {
    //    @Bean
//    @Primary

}
