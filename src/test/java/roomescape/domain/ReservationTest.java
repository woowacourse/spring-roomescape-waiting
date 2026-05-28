package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.support.Fixtures.theme;
import static roomescape.support.Fixtures.time;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidDomainException;
import roomescape.domain.policy.ReservationPolicy;

/**
 * Reservation 도메인 단위 테스트.
 *
 * <p>보호 대상: 도메인 객체의 정합성(자기 자신과 맺은 약속). 외부 의존 없이 입력만으로 결정된다.
 *
 * <p>주의 깊게 볼 설계 결정: "과거 시점 거부"는 여기서 검증하지 않는다.
 * 그 규칙은 Reservation 자신이 아니라 주입된 ReservationPolicy가 책임지기 때문이다.
 * 따라서 과거 거부 규칙의 정확성은 FutureOnlyPolicyTest(정책 단위 테스트)가 검증한다.
 * 여기서는 create()가 정책에 "위임하긴 하는가"만 협력 관점에서 가볍게 확인한다.
 */
class ReservationTest {

    private static final LocalDate DATE = LocalDate.of(2050, 12, 31);

    @Nested
    @DisplayName("이름 제약")
    class NameValidation {

        @Test
        @DisplayName("이름이 null이면 예외")
        void 이름_null() {
            assertThatThrownBy(() -> Reservation.withId(1L, null, DATE, time(1), theme(1)))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약자 이름은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("이름이 공백이면 예외")
        void 이름_공백() {
            assertThatThrownBy(() -> Reservation.withId(1L, "  ", DATE, time(1), theme(1)))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약자 이름은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("이름이 30자를 초과하면 예외")
        void 이름_길이_초과() {
            String tooLong = "가".repeat(31);
            assertThatThrownBy(() -> Reservation.withId(1L, tooLong, DATE, time(1), theme(1)))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약자 이름은 30자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("경계값: 정확히 30자는 허용")
        void 이름_경계값_허용() {
            String exactly30 = "가".repeat(30);
            assertThatCode(() -> Reservation.withId(1L, exactly30, DATE, time(1), theme(1)))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("필수 값 제약")
    class NullValidation {

        @Test
        @DisplayName("날짜가 null이면 예외")
        void 날짜_null() {
            assertThatThrownBy(() -> Reservation.withId(1L, "브라운", null, time(1), theme(1)))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약 날짜는 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("시간이 null이면 예외")
        void 시간_null() {
            assertThatThrownBy(() -> Reservation.withId(1L, "브라운", DATE, null, theme(1)))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약 시간은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("테마가 null이면 예외")
        void 테마_null() {
            assertThatThrownBy(() -> Reservation.withId(1L, "브라운", DATE, time(1), null))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("예약 테마는 비어 있을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("정책 협력")
    class PolicyCollaboration {

        @Test
        @DisplayName("create()는 정책의 생성 검증에 위임한다")
        void create는_정책에_위임한다() {
            RecordingPolicy policy = new RecordingPolicy();

            Reservation.create("브라운", DATE, time(1), theme(1), policy);

            // 호출 순서를 검증하지 않는다 — "위임했다"는 사실만 본다.
            // 과거 거부 규칙의 정확성은 정책 단위 테스트가 책임진다.
            assertThat(policy.creatableCalled).isTrue();
        }

        @Test
        @DisplayName("정책이 거부하면 객체는 생성되지 않는다")
        void 정책_거부시_생성_안됨() {
            ReservationPolicy rejecting = new RejectingPolicy();

            assertThatThrownBy(() -> Reservation.create("브라운", DATE, time(1), theme(1), rejecting))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("정책 거부");
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

    // --- 테스트 전용 정책 스텁 (Mock 라이브러리 없이 의도를 드러내는 수동 스텁) ---

    private static class RecordingPolicy implements ReservationPolicy {
        boolean creatableCalled = false;

        @Override
        public void validateCreatable(LocalDate date, LocalTime time) {
            creatableCalled = true;
        }

        @Override
        public void validateCancellable(LocalDate date, LocalTime time) {
        }

        @Override
        public void validateUpdatable(LocalDate date, LocalTime time) {
        }

        @Override
        public void validateUpdateTarget(LocalDate date, LocalTime time) {
        }
    }

    private static class RejectingPolicy implements ReservationPolicy {
        @Override
        public void validateCreatable(LocalDate date, LocalTime time) {
            throw new IllegalStateException("정책 거부");
        }

        @Override
        public void validateCancellable(LocalDate date, LocalTime time) {
        }

        @Override
        public void validateUpdatable(LocalDate date, LocalTime time) {
        }

        @Override
        public void validateUpdateTarget(LocalDate date, LocalTime time) {
        }
    }
}
