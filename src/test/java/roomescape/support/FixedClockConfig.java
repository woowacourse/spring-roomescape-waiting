package roomescape.support;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import roomescape.domain.policy.ReservationPolicy;
import roomescape.exception.client.BusinessRuleViolationException;

/**
 * 통합/인수 테스트에서 시간을 결정적으로 만들기 위한 고정 Clock 정책 설정.
 *
 * <p>기존에는 이 @TestConfiguration이 ReservationPolicyStepTest와 ErrorResponseStepTest에
 * 그대로 복붙되어 있었다. 시간 통제 전략이 파일마다 달라지는 원인이었으므로 한 곳에 모은다.
 *
 * <p>@Import(FixedClockConfig.class)로 끌어다 쓰면, 운영용 FutureOnlyPolicy(시스템 시계) 대신
 * 고정 시계(2026-05-13 12:00) 기반 정책이 @Primary로 주입된다.
 *
 * <p>"과거/오늘 지난 시간/미래"의 의미가 실행 시점에 따라 흔들리지 않으므로, 시간 의존 케이스를
 * 안정적으로 검증할 수 있다. (단, 과거 거부 "규칙 자체"는 도메인 단위 테스트 FutureOnlyPolicyTest가
 * 더 빠르게 검증한다. 여기서는 그 규칙이 HTTP/서비스 흐름에 연결됐는지를 본다.)
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
        return new FixedClockPolicy(fixed);
    }

    /**
     * 운영용 FutureOnlyPolicy와 동일한 규칙을 갖되, 시계만 외부에서 주입받는 테스트 전용 정책.
     * (운영 FutureOnlyPolicy는 생성자에서 systemDefaultZone을 박아버려서 시계 주입이 불가하다.)
     */
    static class FixedClockPolicy implements ReservationPolicy {

        private final Clock clock;

        FixedClockPolicy(Clock clock) {
            this.clock = clock;
        }

        @Override
        public void validateCreatable(LocalDate date, LocalTime time) {
            if (isPast(date.atTime(time))) {
                throw new BusinessRuleViolationException("지나간 날짜, 시간으로는 예약할 수 없습니다.");
            }
        }

        @Override
        public void validateCancellable(LocalDate date, LocalTime time) {
            if (isPast(date.atTime(time))) {
                throw new BusinessRuleViolationException("이미 지난 예약은 취소할 수 없습니다.");
            }
        }

        @Override
        public void validateUpdatable(LocalDate date, LocalTime time) {
            if (isPast(date.atTime(time))) {
                throw new BusinessRuleViolationException("이미 지난 예약은 변경할 수 없습니다.");
            }
        }

        @Override
        public void validateUpdateTarget(LocalDate date, LocalTime time) {
            if (isPast(date.atTime(time))) {
                throw new BusinessRuleViolationException("지나간 날짜·시간으로는 변경할 수 없습니다.");
            }
        }

        private boolean isPast(LocalDateTime dateTime) {
            return !dateTime.isAfter(LocalDateTime.now(clock));
        }
    }
}
