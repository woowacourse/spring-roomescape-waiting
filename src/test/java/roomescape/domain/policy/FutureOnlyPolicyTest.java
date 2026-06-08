package roomescape.domain.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationDateTime;
import roomescape.exception.client.BusinessRuleViolationException;

/**
 * 시점 검증 규칙(과거 거부)의 단위 테스트.
 *
 * <p>이 미션 테스트 재작성에서 가장 중요한 이동이다.
 * 기존 ReservationPolicyStepTest는 "어제는 거부 / 오늘 지난 시간은 거부 / 내일은 허용"을
 * <b>풀 스프링 컨텍스트 + 실제 H2 + insertTime</b>까지 깔고 검증했다.
 * 하지만 이 규칙은 "외부 상태에 의존하지 않고 입력(날짜·시간)과 현재 시각만으로 결정"된다. 따라서 DB 없이 검증할 수 있고, 그래야 한다. 그래야 "과거/현재/미래"의 의미가 테스트 실행 시점에 흔들리지
 * 않는다.
 *
 * <p>역할(재조정됨): "시점 비교(이후인가)"의 경계 정확성은 이제 ReservationDateTimeTest가 가진다.
 * 이 테스트는 그 비교 위에서 정책이 내리는 판정만 본다.
 *
 * <p>구성은 "무엇을 보호하느냐"로 묶는다(연산별 nest로 쪼개지 않는다):
 * <ul>
 *   <li>연산마다 다른 것 = 메시지뿐 → RejectionMessages: 생성/취소/변경/변경대상을 대등하게 본다</li>
 *   <li>네 연산이 공유하는 것 = 허용·경계 → AllowAndBoundary: 대표(생성)로 한 번만 본다</li>
 * </ul>
 *
 * <p>시간 결정성: 시스템 시계 대신 고정 Clock(2026-05-13 12:00)을 주입한다.
 */
class FutureOnlyPolicyTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 13);
    private static final LocalTime NOW = LocalTime.of(12, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            TODAY.atTime(NOW).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());

    private final ReservationPolicy policy = new FutureOnlyPolicy(FIXED_CLOCK);

    @Nested
    @DisplayName("위반 시 연산별 메시지")
    class RejectionMessages {

        @Test
        @DisplayName("생성: 과거 시점 거부 — 생성 전용 메시지")
        void 생성_거부() {
            assertThatThrownBy(() -> policy.validateCreatable(past()))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("지나간 날짜, 시간으로는 예약할 수 없습니다.");
        }

        @Test
        @DisplayName("취소: 과거 시점 거부 — 취소 전용 메시지")
        void 취소_거부() {
            assertThatThrownBy(() -> policy.validateCancellable(past()))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("이미 지난 예약은 취소할 수 없습니다.");
        }

        @Test
        @DisplayName("변경: 과거 시점 거부 — 변경 전용 메시지")
        void 변경_거부() {
            assertThatThrownBy(() -> policy.validateUpdatable(past()))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("이미 지난 예약은 변경할 수 없습니다.");
        }

        @Test
        @DisplayName("변경 대상: 과거 시점 거부 — 변경 대상 전용 메시지")
        void 변경대상_거부() {
            assertThatThrownBy(() -> policy.validateUpdateTarget(past()))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessage("지나간 날짜, 시간으로는 변경할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("허용/경계 (네 연산 공통)")
    class AllowAndBoundary {

        @Test
        @DisplayName("미래 시점은 허용된다")
        void 미래_허용() {
            assertThatCode(() -> policy.validateCreatable(future()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("정확히 지금은 거부된다 — 엄격히 이후여야 한다")
        void 현재시각_거부() {
            assertThatThrownBy(() -> policy.validateCreatable(ReservationDateTime.of(TODAY, NOW)))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    private static ReservationDateTime past() {
        return ReservationDateTime.of(TODAY.minusDays(1), NOW);
    }

    private static ReservationDateTime future() {
        return ReservationDateTime.of(TODAY.plusDays(1), NOW);
    }
}
