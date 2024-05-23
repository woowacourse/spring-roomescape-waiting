package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WaitingWithSequenceTest {

    @Test
    void 가장_우선_순위가_높은_예약_대기인지_확인() {
        // given
        Member member = new Member("name", "email@email.com", "password123", Role.ADMIN);
        Member member2 = new Member("name", "email@email.com", "password123", Role.ADMIN);
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Theme theme = new Theme("name", "description", "thumbnail");
        Reservation reservation = new Reservation(LocalDate.now(), reservationTime, theme, member);
        Waiting waiting = new Waiting(member2, reservation);
        WaitingWithSequence waitingWithSequence = new WaitingWithSequence(waiting, 1L);

        // when
        boolean result = waitingWithSequence.isPriority();

        // then
        assertThat(result).isTrue();
    }
}
