package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.PastTimeException;

class ReservationTest {

    private static final TimeSlot TIME_SLOT = new TimeSlot(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

    @Test
    @DisplayName("정상적인 값을 입력하면 예약 객체가 생성된다.")
    void 예약_생성() {
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                createSlot(LocalDate.now().plusDays(1)),
                LocalDateTime.now(),
                ReservationStatus.RESERVED
        );
        assertThat(reservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약자 이름이 null이거나 비어있으면 예외가 발생한다.")
    void 예약자_이름_공백_예외_발생() {
        assertThatThrownBy(() -> new Reservation(1L, " ", createSlot(LocalDate.now().plusDays(1)),
                LocalDateTime.now(), ReservationStatus.RESERVED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자 이름은 필수입니다.");
    }

    @Test
    @DisplayName("예약 슬롯이 null이면 예외가 발생한다.")
    void 예약_슬롯_null_예외_발생() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", null, LocalDateTime.now(),
                ReservationStatus.RESERVED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 슬롯은 필수입니다.");
    }

    @Test
    @DisplayName("예약 생성 시각이 null이면 예외가 발생한다.")
    void 예약_생성_시각_null_예외_발생() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", createSlot(LocalDate.now().plusDays(1)), null,
                ReservationStatus.RESERVED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 생성 시각은 필수입니다.");
    }

    @Test
    @DisplayName("예약 상태가 null이면 예외가 발생한다.")
    void 예약_상태_null_예외_발생() {
        assertThatThrownBy(() -> new Reservation(1L, "브라운", createSlot(LocalDate.now().plusDays(1)),
                LocalDateTime.now(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 상태는 필수입니다.");
    }

    @Test
    @DisplayName("예약 날짜와 시간이 생성 시각보다 과거이면 예외가 발생한다.")
    void 생성_시각보다_과거_예약_예외_발생() {
        LocalDate reservationDate = LocalDate.of(2026, 6, 3);
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 3, 11, 0);

        assertThatThrownBy(() -> new Reservation(1L, "브라운", createSlot(reservationDate), createdAt,
                ReservationStatus.RESERVED))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("지난 날짜/시간으로 예약하실 수 없습니다.");
    }

    @Test
    @DisplayName("ID 없이 예약 객체를 생성할 수 있다.")
    void ID_없는_예약_생성() {
        Reservation reservation = new Reservation(null, "브라운", createSlot(LocalDate.now().plusDays(1)),
                LocalDateTime.now(), ReservationStatus.RESERVED);
        assertThat(reservation.getId()).isNull();
    }

    @Test
    @DisplayName("예약 시작 24시간 전보다 이전이면 취소 가능하다.")
    void 예약_시작_24시간_전보다_이전_취소_가능() {
        LocalDate reservationDate = LocalDate.of(2026, 6, 10);
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                createSlot(reservationDate),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                ReservationStatus.RESERVED
        );

        assertThatCode(() -> reservation.validateCancelable(LocalDateTime.of(2026, 6, 9, 9, 59)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 시작 24시간 전부터는 취소할 수 없다.")
    void 예약_시작_24시간_전_취소_불가능() {
        LocalDate reservationDate = LocalDate.of(2026, 6, 10);
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                createSlot(reservationDate),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                ReservationStatus.RESERVED
        );

        assertThatThrownBy(() -> reservation.validateCancelable(LocalDateTime.of(2026, 6, 9, 10, 0)))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("예약 시작 24시간 전까지만 예약을 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("예약 대기는 확정 예약으로 승급할 수 있다.")
    void 예약_대기_승급() {
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                createSlot(LocalDate.of(2026, 6, 10)),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                ReservationStatus.WAITING
        );

        Reservation promoted = reservation.promote();

        assertThat(promoted.isReserved()).isTrue();
    }

    @Test
    @DisplayName("확정된 예약에 대한 승급 요청 시 예외가 발생한다.")
    void 확정_예약_승급_요청_예외_발생() {
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                createSlot(LocalDate.of(2026, 6, 10)),
                LocalDateTime.of(2026, 6, 1, 10, 0),
                ReservationStatus.RESERVED
        );

        assertThatThrownBy(reservation::promote)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 대기만 확정 예약으로 승급할 수 있습니다.");
    }

    private ReservationSlot createSlot(LocalDate date) {
        return new ReservationSlot(1L, date, TIME_SLOT, THEME);
    }
}
