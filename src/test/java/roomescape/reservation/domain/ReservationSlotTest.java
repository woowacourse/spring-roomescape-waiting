package roomescape.reservation.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.reservation.domain.exception.NotSameSlotException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationSlotTest {

    private static Member createMember() {
        return Member.signUpUser(
                "user1",
                Email.create("user1@email.com"),
                Password.encrypt("1234", rawPassword -> rawPassword)
        );
    }

    private static Member createMember2() {
        return Member.signUpUser(
                "user2",
                Email.create("user2@email.com"),
                Password.encrypt("1234", rawPassword -> rawPassword)
        );
    }

    private static ReservationDate createReservationDate() {
        return new ReservationDate(LocalDate.now().plusDays(1));
    }

    private static ReservationTime createReservationTime() {
        return ReservationTime.open(LocalTime.of(10, 0));
    }

    private static ReservationDateTime createReservationDateTime() {
        return ReservationDateTime.create(
                createReservationDate(),
                createReservationTime()
        );
    }

    private static Theme createTheme() {
        return Theme.create("공포", "공포 테마 설명", "공포 테마 이미지 URL");
    }

    private static Theme createTheme2() {
        return Theme.create("모험", "모험 테마 설명", "모험 테마 이미지 URL");
    }

    @Test
    void 같은_슬롯의_예약이_아니면_예외가_발생한다() {
        Member reserver1 = createMember();
        Member reserver2 = createMember2();
        ReservationDateTime reservationDateTime = createReservationDateTime();
        Theme theme = createTheme();
        Theme theme2 = createTheme2();
        Reservation reservation1 = Reservation.reserve(
                reserver1, reservationDateTime, theme, LocalDateTime.now()
        );
        Reservation reservation2 = Reservation.wait(
                reserver2, reservationDateTime, theme2, LocalDateTime.now()
        );

        assertThatThrownBy(() -> new ReservationSlot(List.of(reservation1, reservation2)))
                .isInstanceOf(NotSameSlotException.class)
                .hasMessage("같은 슬롯의 예약이 아닙니다.");
    }

    @Test
    void 슬롯_내에서_예약의_순서를_구한다() {
        Member reserver1 = createMember();
        Member reserver2 = createMember2();
        ReservationDateTime reservationDateTime = createReservationDateTime();
        Theme theme = createTheme();
        Reservation reservation1 = Reservation.reserve(
                reserver1, reservationDateTime, theme, LocalDateTime.now()
        );
        Reservation reservation2 = Reservation.wait(
                reserver2, reservationDateTime, theme, LocalDateTime.now()
        );

        ReservationSlot reservationSlot = new ReservationSlot(List.of(reservation1, reservation2));

        assertThat(reservationSlot.isFirst(reservation1)).isTrue();
        assertThat(reservationSlot.getOrder(reservation2)).isEqualTo(1);
    }

    @Test
    void 슬롯_내에_대기중인_예약이_있는지_확인한다() {
        Member reserver1 = createMember();
        Member reserver2 = createMember2();
        ReservationDateTime reservationDateTime = createReservationDateTime();
        Theme theme = createTheme();
        Reservation reservation1 = Reservation.reserve(
                reserver1, reservationDateTime, theme, LocalDateTime.now()
        );
        Reservation reservation2 = Reservation.wait(
                reserver2, reservationDateTime, theme, LocalDateTime.now()
        );

        ReservationSlot reservationSlot = new ReservationSlot(List.of(reservation1, reservation2));

        assertThat(reservationSlot.hasWaiting()).isTrue();
    }

    @Test
    void 다음_예약을_가져온다() {
        Member reserver1 = createMember();
        Member reserver2 = createMember2();
        ReservationDateTime reservationDateTime = createReservationDateTime();
        Theme theme = createTheme();
        Reservation reservation1 = Reservation.reserve(
                reserver1, reservationDateTime, theme, LocalDateTime.now()
        );
        Reservation reservation2 = Reservation.wait(
                reserver2, reservationDateTime, theme, LocalDateTime.now()
        );

        ReservationSlot reservationSlot = new ReservationSlot(List.of(reservation1, reservation2));

        assertThat(reservationSlot.getNext(reservation1)).isEqualTo(reservation2);
    }
}
