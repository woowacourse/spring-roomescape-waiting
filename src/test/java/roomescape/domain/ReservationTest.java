package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.ReservationStatus.CONFIRMED;
import static roomescape.support.Fixtures.theme;
import static roomescape.support.Fixtures.time;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidDomainException;
import roomescape.domain.policy.FutureOnlyPolicy;
import roomescape.domain.policy.ReservationPolicy;
import roomescape.exception.client.BusinessRuleViolationException;

/**
 * Reservation 도메인 단위 테스트.
 *
 * <p>보호 대상: 도메인 객체의 정합성(자기 자신과 맺은 약속)과 자기 시점의 합성.
 * 외부 의존(DB·스프링) 없이 입력만으로 결정된다.
 *
 * <p>역할 분담(중요):
 * <ul>
 *   <li>시점 비교(이후인가)의 경계 정확성 → ReservationDateTimeTest (값 객체 단위)</li>
 *   <li>과거 거부 규칙의 연산별 메시지·판정 → FutureOnlyPolicyTest (정책 단위)</li>
 *   <li>create가 정책 결과를 객체 생성에 반영하는 연결 → 여기 (정책 협력)</li>
 * </ul>
 *
 * <p>정책 협력은 수동 스텁이 아니라 실제 정책(FutureOnlyPolicy)을 고정 Clock과 함께 주입해
 * 결과(예외/생성 성공)로 검증한다. 이전엔 RecordingPolicy로 "호출했는가"(행위)를 봤는데 그건
 * 런던파에 가까웠다. 고전파를 일관되게 따르려 "거부 시점이면 생성이 실패한다"는 결과만 본다.
 * 같은 정책이 FutureOnlyPolicyTest에도 쓰이지만 보호 대상이 다르다 — 거기선 규칙의 경계·메시지를,
 * 여기선 그 결과가 create에 연결되는지를 본다.
 */
class ReservationTest {

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);

    @Nested
    @DisplayName("이름 제약")
    class NameValidation {

        @Test
        @DisplayName("이름이 null이면 예외")
        void 이름_null() {
            assertThatThrownBy(() -> Reservation.withId(1L, null, DATE, time(1), theme(1), CONFIRMED))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약자 이름은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("이름이 공백이면 예외")
        void 이름_공백() {
            assertThatThrownBy(() -> Reservation.withId(1L, "  ", DATE, time(1), theme(1), CONFIRMED))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약자 이름은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("이름이 30자를 초과하면 예외")
        void 이름_길이_초과() {
            String tooLong = "가".repeat(31);
            assertThatThrownBy(() -> Reservation.withId(1L, tooLong, DATE, time(1), theme(1), CONFIRMED))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약자 이름은 30자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("경계값: 정확히 30자는 허용")
        void 이름_경계값_허용() {
            String exactly30 = "가".repeat(30);
            assertThatCode(() -> Reservation.withId(1L, exactly30, DATE, time(1), theme(1), CONFIRMED))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("필수 값 제약")
    class NullValidation {

        @Test
        @DisplayName("날짜가 null이면 예외")
        void 날짜_null() {
            assertThatThrownBy(() -> Reservation.withId(1L, "브라운", null, time(1), theme(1), CONFIRMED))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약 날짜는 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("시간이 null이면 예외")
        void 시간_null() {
            assertThatThrownBy(() -> Reservation.withId(1L, "브라운", DATE, null, theme(1), CONFIRMED))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약 시간은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("테마가 null이면 예외")
        void 테마_null() {
            assertThatThrownBy(() -> Reservation.withId(1L, "브라운", DATE, time(1), null, CONFIRMED))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약 테마는 비어 있을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("예약 시점 노출")
    class DateTime {

        @Test
        @DisplayName("dateTime()은 예약의 날짜+시작시각을 시점으로 합성한다")
        void 시점_합성() {
            Reservation reservation = Reservation.withId(
                    1L, "브라운", LocalDate.of(2050, 12, 31), time(1, LocalTime.of(10, 0)),
                    theme(1), CONFIRMED);

            // 시점이 2050-12-31 10:00임을 행위로 확인 - getter로 들여다보지 않는다
            assertThat(reservation.dateTime()
                    .startsAtOrBefore(LocalDate.of(2050, 12, 31).atTime(9, 59))).isFalse();
            assertThat(reservation.dateTime()
                    .startsAtOrBefore(LocalDate.of(2050, 12, 31).atTime(10, 1))).isTrue();
        }
    }


    @Nested
    @DisplayName("정책 협력 (생성,삭제,업데이트)결과 검증")
    class PolicyCollaboration {
        //
        private final Clock dateIsPast = fixedAt(DATE.plusYears(1));
        private final Clock dateIsFuture = fixedAt(DATE.minusYears(1));

        @Test
        @DisplayName("정책이 허용하는 시점이면 객체가 생성된다")
        void 허용_시점_생성_성공() {
            ReservationPolicy policy = new FutureOnlyPolicy(dateIsFuture);

            assertThatCode(() -> Reservation.create("브라운", DATE, time(1), theme(1), policy))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("정책이 거부하는 시점이면 객체 생성이 실패한다")
        void 거부_시점_생성_실패() {
            ReservationPolicy policy = new FutureOnlyPolicy(dateIsPast);

            assertThatThrownBy(() -> Reservation.create("브라운", DATE, time(1), theme(1), policy))
                    .isInstanceOf(BusinessRuleViolationException.class);
            // 메시지·경계는 여기서 보지 않는다 — FutureOnlyPolicyTest의 책임이다.
            // 여기서는 "정책 결과가 생성 성공/실패로 반영된다"는 연결만 본다.
        }

        private Clock fixedAt(LocalDate date) {
            return Clock.fixed(
                    date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault());
        }
    }

    @Nested
    @DisplayName("대기 승격")
    class Promotion {

        @Test
        @DisplayName("promote()는 대기 정보를 보존한 예약을 만들고, id는 새로 발급되므로 null이다")
        void 대기를_예약으로_승격() {
            Waiting waiting = Waiting.withId(100L, "콘", DATE, time(1), theme(1), 1);

            Reservation promoted = Reservation.promote(waiting);

            assertThat(promoted.getName()).isEqualTo("콘");
            assertThat(promoted.getDate()).isEqualTo(DATE);
            assertThat(promoted.getTime().getId()).isEqualTo(1L);
            assertThat(promoted.getId()).isNull();
        }
    }

}
