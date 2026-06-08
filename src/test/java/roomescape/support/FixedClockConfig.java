package roomescape.support;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.domain.policy.FutureOnlyPolicy;
import roomescape.domain.policy.ReservationPolicy;

/**
 * 통합/인수 테스트에서 시간을 결정적으로 만들기 위한 고정 Clock 정책 설정.
 *
 * <p>기존에는 이 @TestConfiguration이 ReservationPolicyStepTest와 ErrorResponseStepTest에
 * 그대로 복붙되어 있었다. 시간 통제 전략이 파일마다 달라지는 원인이었으므로 한 곳에 모은다.
 *
 * <p>@Import(FixedClockConfig.class)로 끌어 쓰면, 운영 FutureOnlyPolicy가 시스템 시계 대신 고정 시계(2026-05-13 12:00)를 받은 정책 인스턴스로
 * (우선)@Primary 주입된다. 운영 정책 클래스를 그대로 * 쓰므로 "테스트가 검증하는 규칙 = 운영이 실제로 쓰는 규칙"이 보장된다.
 *
 * <p>Clock 자체가 아니라 "정책 빈"을 @Primary로 두는 이유: FixedPopularPolicyConfig도 자기 * 기준일(2026-05-09)로 시계를 고정하는데,
 * 두 설정이 각각(우선)@Primary Clock을 등록하면 한 컨텍스트에 * 함께 올라올 때 충돌한다. 시계 고정을 정책 빈 단위로 캡슐화하면 그 충돌이 구조적으로 사라진다.
 *
 * <p>"과거/오늘 지난 시간/미래"의 의미가 실행 시점에 따라 흔들리지 않으므로, 시간 의존 케이스를 안정적으로 검증할 수 있다.
 * (단, 과거 거부 "규칙 자체"는 도메인 단위 테스트 FutureOnlyPolicyTest가 더 빠르게 검증한다. 여기서는 그 규칙이 HTTP/서비스 흐름에 연결됐는지를 본다.)
 */
@TestConfiguration
public class FixedClockConfig {

    public static final LocalDate TODAY = LocalDate.of(2026, 5, 13);
    public static final LocalTime NOW = LocalTime.of(12, 0);

    @Bean
    @Primary
    public ReservationPolicy fixedReservationPolicy() {
        Clock fixed = Clock.fixed(
                TODAY.atTime(NOW).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        return new FutureOnlyPolicy(fixed);
    }
}
