package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationTest {

    private final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = Theme.of("우테코", "우테코 전용 테마", "https://example.com");

    @Test
    @DisplayName("성공적으로 예약 도메인 객체를 생성한다.")
    void construct_validInput_returnsReservation() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime requestTime = LocalDateTime.now();

        // when & then
        assertThatCode(() -> new Reservation("브라운", new ReservationSlot(date, reservationTime, theme), requestTime))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("과거 날짜로 예약을 생성하려 하면 InvalidBusinessStateException을 던진다.")
    void construct_pastDate_throwsInvalidBusinessStateException() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDateTime requestTime = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> new Reservation("브라운", new ReservationSlot(pastDate, reservationTime, theme), requestTime))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("과거 시간으로 예약을 생성하려 하면 InvalidBusinessStateException을 던진다.")
    void construct_pastTime_throwsInvalidBusinessStateException() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime pastTimeVal = LocalTime.of(10, 0);
        ReservationTime pastTime = new ReservationTime(2L, pastTimeVal);
        LocalDateTime requestTime = today.atTime(11, 0);

        // when & then
        assertThatThrownBy(() -> new Reservation("브라운", new ReservationSlot(today, pastTime, theme), requestTime))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 소유자 이름이 일치하면 예외가 발생하지 않는다.")
    void validateOwner_matchName_doesNotThrow() {
        // given
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), reservationTime, theme),
                LocalDate.now().plusDays(1).atStartOfDay(), true);

        // when & then
        assertThatCode(() -> reservation.validateDeletableByUser("브라운", LocalDateTime.now()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 소유자 이름이 일치하지 않으면 ForbiddenException을 던진다.")
    void validateOwner_mismatchName_throwsForbiddenException() {
        // given
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), reservationTime, theme),
                LocalDate.now().plusDays(1).atStartOfDay(), true);

        // when & then
        assertThatThrownBy(() -> reservation.validateDeletableByUser("코니", java.time.LocalDateTime.now()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("예약 일자가 과거인 상태에서 validateExpiry 실행 시 InvalidBusinessStateException을 던진다.")
    void validateExpiry_pastDate_throwsInvalidBusinessStateException() {
        // given
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().minusDays(1), reservationTime, theme),
                LocalDate.now().minusDays(1).atStartOfDay(), true);
        LocalDateTime requestTime = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> reservation.validateDeletableByUser("브라운", requestTime))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 시간(시각)이 과거인 상태에서 validateExpiry 실행 시 InvalidBusinessStateException을 던진다.")
    void validateExpiry_pastTime_throwsInvalidBusinessStateException() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime pastTimeVal = LocalTime.of(10, 0);
        ReservationTime pastTime = new ReservationTime(2L, pastTimeVal);
        Reservation reservation = new Reservation(1L, "브라운", new ReservationSlot(today, pastTime, theme),
                today.atTime(9, 0), true);
        LocalDateTime requestTime = today.atTime(11, 0);

        // when & then
        assertThatThrownBy(() -> reservation.validateDeletableByUser("브라운", requestTime))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("소유자가 동일하고 날짜가 유효하면 성공적으로 예약을 수정한다.")
    void update_validInput_returnsUpdatedReservation() {
        // given
        Reservation original = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), reservationTime, theme),
                LocalDate.now().plusDays(1).atStartOfDay(), true);
        LocalDate newDate = LocalDate.now().plusDays(2);
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(14, 0));
        LocalDateTime requestTime = LocalDateTime.now();

        // when
        Reservation updated = original.update(newDate, newTime, "브라운", requestTime);

        // then
        assertThat(updated.getDate()).isEqualTo(newDate);
        assertThat(updated.getTime()).isEqualTo(newTime);
    }

    @Test
    @DisplayName("소유자가 아닌 사람이 수정을 요청하면 ForbiddenException을 던진다.")
    void update_mismatchOwner_throwsForbiddenException() {
        // given
        Reservation original = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), reservationTime, theme),
                LocalDate.now().plusDays(1).atStartOfDay(), true);
        LocalDateTime requestTime = LocalDateTime.now();
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(14, 0));

        // when & then
        assertThatThrownBy(() -> original.update(LocalDate.now().plusDays(2), newTime, "코니", requestTime))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("이미 만료된 예약건에 대해 수정을 요청하면 InvalidBusinessStateException을 던진다.")
    void update_expiredOriginalReservation_throwsInvalidBusinessStateException() {
        // given
        Reservation original = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().minusDays(1), reservationTime, theme),
                LocalDate.now().minusDays(1).atStartOfDay(), true);
        LocalDateTime requestTime = LocalDateTime.now();
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(14, 0));

        // when & then
        assertThatThrownBy(() -> original.update(LocalDate.now().plusDays(1), newTime, "브라운", requestTime))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("수정하려는 타겟 시간대가 이미 지난 과거인 경우 InvalidBusinessStateException을 던진다.")
    void update_expiredNewDate_throwsInvalidBusinessStateException() {
        // given
        Reservation original = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), reservationTime, theme),
                LocalDate.now().plusDays(1).atStartOfDay(), true);
        LocalDateTime requestTime = LocalDateTime.now();
        ReservationTime newTime = new ReservationTime(2L, LocalTime.of(14, 0));

        // when & then
        assertThatThrownBy(() -> original.update(LocalDate.now().minusDays(1), newTime, "브라운", requestTime))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }
}
