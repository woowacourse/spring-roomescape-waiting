package roomescape.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.domain.PopularThemePolicy;
import roomescape.domain.WeeklyTopTenPopularThemePolicy;

@Configuration
public class PopularThemePolicyConfig {

    @Bean
    public PopularThemePolicy popularThemePolicy() {
        return new WeeklyTopTenPopularThemePolicy();
    }
}
