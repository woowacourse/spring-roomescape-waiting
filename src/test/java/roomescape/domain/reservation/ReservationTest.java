package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.slot.ReservationSlot;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.Theme;
import roomescape.exception.RoomEscapeBusinessException;

class ReservationTest {

    @DisplayName("이미 예약을 한 사람은 예약 대기를 할 수 없다.")
    @Test
    void addWaitingByDuplicateReservationMember() {
        // given
        Member member = new Member(1L, "비밥", "uu@naver.com", "1234", Role.USER);
        LocalDate date = LocalDate.parse("2024-06-01");
        ReservationTime time = new ReservationTime(LocalTime.parse("10:00"));
        Theme theme = new Theme("테마이름", "테마 상세", "테마 섬네일");
        ReservationSlot reservationSlot = new ReservationSlot(date, time, theme);

        Reservation reservation = new Reservation(1L, member, reservationSlot);

        // when // then
        assertThatThrownBy(() -> reservation.addWaiting(member))
                .isInstanceOf(RoomEscapeBusinessException.class)
                .hasMessage("중복된 예약을 할 수 없습니다.");
    }

    @DisplayName("이미 예약 대기를 한 사람은 예약 대기를 할 수 없다.")
    @Test
    void addWaitingByDuplicateWaitingMember() {
        // given
        Member member = new Member(1L, "비밥", "uu@naver.com", "1234", Role.USER);
        Member member1 = new Member(2L, "비밥1", "uu1@naver.com", "1234", Role.USER);

        LocalDate date = LocalDate.parse("2024-06-01");
        ReservationTime time = new ReservationTime(LocalTime.parse("10:00"));
        Theme theme = new Theme("테마이름", "테마 상세", "테마 섬네일");
        ReservationSlot reservationSlot = new ReservationSlot(date, time, theme);

        Reservation reservation = new Reservation(1L, member, reservationSlot);
        reservation.addWaiting(member1);

        // when // then
        assertThatThrownBy(() -> reservation.addWaiting(member1))
                .isInstanceOf(RoomEscapeBusinessException.class)
                .hasMessage("중복된 예약을 할 수 없습니다.");
    }

    @DisplayName("예약 대기를 한다.")
    @Test
    void addWaiting() {
        // given
        Member member = new Member(1L, "비밥", "uu@naver.com", "1234", Role.USER);
        Member member1 = new Member(2L, "비밥1", "uu1@naver.com", "1234", Role.USER);
        Member member2 = new Member(3L, "비밥3", "uu3@naver.com", "1234", Role.USER);

        LocalDate date = LocalDate.parse("2024-06-01");
        ReservationTime time = new ReservationTime(LocalTime.parse("10:00"));
        Theme theme = new Theme("테마이름", "테마 상세", "테마 섬네일");
        ReservationSlot reservationSlot = new ReservationSlot(date, time, theme);

        Reservation reservation = new Reservation(1L, member, reservationSlot);

        // when
        Waiting waiting1 = reservation.addWaiting(member1);
        Waiting waiting2 = reservation.addWaiting(member2);

        // then
        assertAll(
                () -> assertThat(waiting1.getMember()).isEqualTo(member1),
                () -> assertThat(waiting1.getReservation()).isEqualTo(reservation),
                () -> assertThat(waiting2.getMember()).isEqualTo(member2),
                () -> assertThat(waiting2.getReservation()).isEqualTo(reservation)
        );
    }

    @DisplayName("예약 대기자가 없으면 예약 대기를 승인할 수 없다.")
    @Test
    void approveEmptyWaiting() {
        // given
        Member member = new Member(1L, "비밥", "uu@naver.com", "1234", Role.USER);
        LocalDate date = LocalDate.parse("2024-06-01");
        ReservationTime time = new ReservationTime(LocalTime.parse("10:00"));
        Theme theme = new Theme("테마이름", "테마 상세", "테마 섬네일");
        ReservationSlot reservationSlot = new ReservationSlot(date, time, theme);

        Reservation reservation = new Reservation(1L, member, reservationSlot);

        // when // then
        assertThatThrownBy(reservation::approveWaiting)
                .isInstanceOf(RoomEscapeBusinessException.class)
                .hasMessage("예약 대기자가 없습니다.");
    }

    @DisplayName("예약 대기를 승인하면 첫 번째 예약 대기자가 예약된다.")
    @Test
    void approveWaiting() {
        // given
        Member member = new Member(1L, "비밥", "uu@naver.com", "1234", Role.USER);
        Member member1 = new Member(2L, "비밥1", "uu1@naver.com", "1234", Role.USER);
        Member member2 = new Member(3L, "비밥3", "uu3@naver.com", "1234", Role.USER);

        LocalDate date = LocalDate.parse("2024-06-01");
        ReservationTime time = new ReservationTime(LocalTime.parse("10:00"));
        Theme theme = new Theme("테마이름", "테마 상세", "테마 섬네일");
        ReservationSlot reservationSlot = new ReservationSlot(date, time, theme);

        Reservation reservation = new Reservation(1L, member, reservationSlot);
        reservation.addWaiting(member1);
        reservation.addWaiting(member2);

        // when
        reservation.approveWaiting();

        // then
        assertAll(
                () -> assertThat(reservation.getMember()).isEqualTo(member1),
                () -> assertThat(reservation).extracting("waitings").asList()
                        .hasSize(1).extracting("member")
                        .containsExactly(member2)
        );
    }
}
