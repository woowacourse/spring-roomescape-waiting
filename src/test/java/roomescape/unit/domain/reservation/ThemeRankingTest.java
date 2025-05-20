package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRanking;

class ThemeRankingTest {

    @Test
    void 테마_랭킹_테스트() {
        Theme themeA = new Theme(1L, "asdf", "asdf", "asdf");
        Theme themeB = new Theme(2L, "asdf", "asdf", "asdf");
        Reservation reservation = new Reservation(0L, "tuda", LocalDate.now(), new ReservationTime(0L, LocalTime.now()),
                themeA);

        Reservation reservation2 = new Reservation(1L, "tuda2", LocalDate.now(),
                new ReservationTime(0L, LocalTime.now()), themeB);

        Reservation reservation3 = new Reservation(2L, "tuda3", LocalDate.now(),
                new ReservationTime(0L, LocalTime.now()), themeB);

        Reservation reservation4 = new Reservation(2L, "tuda3", LocalDate.now(),
                new ReservationTime(0L, LocalTime.now()), themeB);

        ThemeRanking ranking = new ThemeRanking(List.of(reservation, reservation2, reservation3, reservation4));
        assertThat(ranking.getAscendingRanking()).containsExactlyElementsOf(List.of(themeB, themeA));
    }
}
