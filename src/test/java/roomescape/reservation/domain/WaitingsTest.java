package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

class WaitingsTest {

    @Test
    @DisplayName("예약 대기 순위를 구한다.")
    void findMemberRankTest() {
        Theme theme = new Theme(1L, "a", "a", "a");
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
        Member member1 = new Member(1L, Role.USER, "hogi", "a", "a");
        Member member2 = new Member(2L, Role.USER, "kaki", "a", "a");
        Member realMember = new Member(3L, Role.USER, "sangdol", "a", "a");
        Reservation reservation1 = new Reservation(1L, LocalDate.now(), Status.WAITING, member1, theme, reservationTime,
                LocalDateTime.now());
        Reservation reservation2 = new Reservation(2L, LocalDate.now(), Status.WAITING, member2, theme, reservationTime,
                LocalDateTime.now().plusHours(1));
        Reservation reservation3 = new Reservation(3L, LocalDate.now(), Status.WAITING, realMember, theme,
                reservationTime,
                LocalDateTime.now().plusDays(1));
        List<Reservation> waiting = new ArrayList<>();
        waiting.add(reservation1);
        waiting.add(reservation2);
        waiting.add(reservation3);
        Waitings waitings = new Waitings(waiting);

        Assertions.assertThat(waitings.findMemberRank(reservation3, 3L)).isEqualTo(3);
    }

}
