package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    public Password PASSWORD = new Password("password", "salt");
    public Member MEMBER = new Member(1L, "test@email.com", PASSWORD, "name", Role.USER);
    public Theme THEME = new Theme(1L, "name", "description", "thumbnail");
    public ReservationTime RESERVATION_TIME = new ReservationTime(1L, LocalTime.parse("10:00"));

    @DisplayName("예약 대기중인 예약을 예약상태로 변경할 수 있다.")
    @Test
    void given_waitingReservation_when_changeToReserved_then_reservationsStatusIsReserved() {
        //given
        Reservation reservation = new Reservation(1L, MEMBER, LocalDate.parse("2999-01-01"), RESERVATION_TIME, THEME, ReservationStatus.WAITING);
        //when
        reservation.changeToReserved();
        final ReservationStatus status = reservation.getStatus();
        //then
        assertThat(status).isEqualTo(ReservationStatus.RESERVED);
    }


    @DisplayName("확정된 예약을 생성할 수 있다.")
    @Test
    void given_when_reserved_then_statusIsReserved() {
        //given
        Reservation reservation = Reservation.reserved(MEMBER, LocalDate.parse("2999-01-01"), RESERVATION_TIME, THEME);
        //when
        final ReservationStatus status = reservation.getStatus();
        //then
        assertThat(status).isEqualTo(ReservationStatus.RESERVED);
    }

    @DisplayName("대기중인 예약을 생성할 수 있다.")
    @Test
    void given_when_waiting_then_statusIsReserved() {
        //given
        Reservation reservation = Reservation.waiting(MEMBER, LocalDate.parse("2999-01-01"), RESERVATION_TIME, THEME);
        //when
        final ReservationStatus status = reservation.getStatus();
        //then
        assertThat(status).isEqualTo(ReservationStatus.WAITING);
    }
}