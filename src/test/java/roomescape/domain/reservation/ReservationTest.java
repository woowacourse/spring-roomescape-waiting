package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;

class ReservationTest {

    @DisplayName("예약이 이미 존재하면 예약 대기 상태이다.")
    @Test
    void createWait() {
        // given
        Member member = Member.createUser("감자", "email@email.com", "1234");
        ReservationTime time = new ReservationTime(LocalTime.parse("10:00"));
        Theme theme = new Theme("이름1", "설명1", "섬네일");

        boolean isAlreadyBooked = true;

        // when
        Reservation reservation = Reservation.create(member, LocalDate.now(), time, theme, isAlreadyBooked);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.WAIT);
    }

    @DisplayName("예약이 존재하지 않으면 예약 상태이다.")
    @Test
    void createBooked() {
        // given
        Member member = Member.createUser("감자", "email@email.com", "1234");
        ReservationTime time = new ReservationTime(LocalTime.parse("10:00"));
        Theme theme = new Theme("이름1", "설명1", "섬네일");

        boolean isAlreadyBooked = false;

        // when
        Reservation reservation = Reservation.create(member, LocalDate.now(), time, theme, isAlreadyBooked);

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.BOOKED);
    }
}
