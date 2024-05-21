package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.slot.ReservationSlot;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.Theme;

class WaitingRanksTest {

    @DisplayName("예약 대기 순번을 구한다.")
    @Test
    void countRank() {
        // given
        Member member = new Member(1L, "비밥", "uu@naver.com", "1234", Role.USER);
        Member member1 = new Member(2L, "비밥1", "uu1@naver.com", "1234", Role.USER);
        Member member2 = new Member(3L, "비밥3", "uu3@naver.com", "1234", Role.USER);

        LocalDate date = LocalDate.parse("2024-06-01");
        ReservationTime time = new ReservationTime(LocalTime.parse("10:00"));
        Theme theme = new Theme("테마이름", "테마 상세", "테마 섬네일");
        ReservationSlot reservationSlot = new ReservationSlot(date, time, theme);

        Reservation reservation1 = new Reservation(1L, member, reservationSlot);
        Reservation reservation2 = new Reservation(2L, member, reservationSlot);
        Waiting waiting1 = new Waiting(1L, member1, reservation1);
        Waiting waiting2 = new Waiting(2L, member2, reservation1);
        Waiting waiting3 = new Waiting(3L, member2, reservation2);

        List<Waiting> totalWaitings = new ArrayList<>();
        totalWaitings.add(waiting1);
        totalWaitings.add(waiting2);
        totalWaitings.add(waiting3);


        // when
        WaitingRanks waitingRanks = WaitingRanks.of(totalWaitings, member2);

        // then
        Map<Waiting, Integer> waitingRanksMap = waitingRanks.getWaitingRanks();

        assertAll(
                () -> assertThat(waitingRanksMap.get(waiting2)).isEqualTo(2),
                () -> assertThat(waitingRanksMap.get(waiting3)).isEqualTo(1)
        );
    }
}
