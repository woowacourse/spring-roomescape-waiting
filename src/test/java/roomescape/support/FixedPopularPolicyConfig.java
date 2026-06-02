package roomescape.support;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.domain.policy.PopularThemePolicy;

/**
 * 인기 테마 집계의 시간 기준(오늘)을 고정하기 위한 테스트 설정.
 *
 * <p>findPopular는 "오늘로부터 최근 7일"을 집계한다. 시스템 시계를 쓰면 "어떤 예약이 집계 범위에
 * 드는가"가 테스트 실행 날짜마다 달라져 결과가 흔들린다. 그래서 today를 2026-05-09로 고정한다.
 *
 * <p>운영 RecentWeekPopularPolicy는 생성자에서 systemDefaultZone을 박아 시계 주입이 불가하므로,
 * 동일 규칙을 갖되 시계만 주입받는 TestRecentWeekPopularPolicy를 @Primary로 올린다.
 * (FixedClockConfig가 ReservationPolicy를 고정하는 것과 같은 패턴이다.)
 */
@TestConfiguration
public class FixedPopularPolicyConfig {

    public static final LocalDate TODAY = LocalDate.of(2026, 5, 9);

    @Bean
    @Primary
    public PopularThemePolicy fixedPopularThemePolicy() {
        Clock fixed = Clock.fixed(
                TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        return new TestRecentWeekPopularPolicy(fixed);
    }
}
