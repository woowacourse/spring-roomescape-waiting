package roomescape.support;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.domain.policy.PopularThemePolicy;
import roomescape.domain.policy.RecentWeekPopularPolicy;

/**
 * 인기 테마 집계의 시간 기준(오늘)을 고정하기 위한 테스트 설정.
 *
 * <p>findPopular는 "오늘로부터 최근 7일"을 집계한다. 시스템 시계를 쓰면 "어떤 예약이 집계 범위에
 * 드는가"가 테스트 실행 날짜마다 달라져 결과가 흔들린다. 그래서 today를 2026-05-09로 고정한다.
 *
 * <p>운영 RecentWeekPopularPolicy에 고정 Clock을 주입한 인스턴스를 @Primary로 올린다.
 * 운영 정책 클래스를 그대로 쓰므로(시계만 고정), 테스트가 검증하는 집계 기준이 운영이 실제로 쓰는 기준과 동일함이 보장된다.
 *
 * <p>Clock 자체가 아니라 "정책 빈"을 @Primary로 두는 이유: FixedClockConfig도 자기 기준시각(2026-05-13 12:00)으로 시계를 고정하는데,
 * 두 설정이 각각 @Primary Clock을 등록하면 한 컨텍스트에 함께 올라올 때 충돌한다. 시계 고정을 정책 빈 단위로 캡슐화하면 그 충돌이 구조적으로 사라진다. FixedClockConfig가
 * ReservationPolicy를 고정하는 것과 같은 패턴이다.
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
        return new RecentWeekPopularPolicy(fixed);
    }
}
