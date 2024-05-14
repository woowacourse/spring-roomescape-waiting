package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.dto.ReservationRequest;
import roomescape.exception.ReservationFailException;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

class ReservationCreateValidatorTest {
    private final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-11-22"), 1L, 2L, 1L);
    private final TimeSlot timeSlot = new TimeSlot(reservationRequest.timeId(), LocalTime.parse("10:00"));
    private final Theme theme = new Theme(reservationRequest.themeId(), "themeName", "description", "thumbnail");
    private final Member member = new Member(reservationRequest.memberId(), "poke@test.com", "poke", "role");

    @DisplayName("예약 요청 값, 테마, 시간 객체를 통해 객체 생성에 성공한다.")
    @Test
    void given_when_new_then_doesNotException() {
        //when, then
        assertThatCode(() -> new ReservationCreateValidator(reservationRequest, timeSlot, theme, member)).doesNotThrowAnyException();
    }

    @DisplayName("예약 날짜가 이미 지난 날이면 예약에 실패한다.")
    @Test
    void given_pastReservationRequest_when_new_then_thrownReservationFailException() {
        //given
        ReservationRequest pastReservationRequest = new ReservationRequest(LocalDate.parse("1999-11-22"), 1L, 2L, 1L);
        TimeSlot timeSlot = new TimeSlot(pastReservationRequest.timeId(), LocalTime.parse("10:00"));
        //when, then
        assertThatThrownBy(() -> new ReservationCreateValidator(pastReservationRequest, timeSlot, theme, member)).isInstanceOf(ReservationFailException.class);
    }

    @DisplayName("객체가 생성되면 예약 객체를 반환할 수 있다.")
    @Test
    void given_reservationCreateValidator_when_create_then_returnReservation() {
        //given
        final ReservationCreateValidator reservationCreateValidator = new ReservationCreateValidator(reservationRequest, timeSlot, theme, member);
        //when, then
        assertThat(reservationCreateValidator.create()).isInstanceOf(Reservation.class);
    }
}