package roomescape.unit.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.ThemeRanking;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.global.ReservationStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeRankingTest {

    @Test
    void 예약_수에_따라_테마가_내림차순으로_정렬된다() {
        // given
        Theme themeA = new Theme(1L, "A", "a", "a");
        Theme themeB = new Theme(2L, "B", "b", "b");
        Theme themeC = new Theme(3L, "C", "c", "c");

        List<Reservation> reservations = new ArrayList<>();

        reservations.add(new Reservation(null, LocalDate.now(), new ReservationTime(LocalTime.now()), themeB,
                ReservationStatus.RESERVED));
        reservations.add(new Reservation(null, LocalDate.now(), new ReservationTime(LocalTime.now()), themeB,
                ReservationStatus.RESERVED));
        reservations.add(new Reservation(null, LocalDate.now(), new ReservationTime(LocalTime.now()), themeB,
                ReservationStatus.RESERVED));

        reservations.add(new Reservation(null, LocalDate.now(), new ReservationTime(LocalTime.now()), themeA,
                ReservationStatus.RESERVED));
        reservations.add(new Reservation(null, LocalDate.now(), new ReservationTime(LocalTime.now()), themeA,
                ReservationStatus.RESERVED));

        reservations.add(new Reservation(null, LocalDate.now(), new ReservationTime(LocalTime.now()), themeC,
                ReservationStatus.RESERVED));

        ThemeRanking ranking = new ThemeRanking(reservations);

        // when
        List<Theme> result = ranking.getAscendingRanking();

        // then
        List<Theme> expected = List.of(themeB, themeA, themeC);
        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    void 랭킹은_10개_까지로_제한한다() {
        // given
        List<Reservation> reservations = new ArrayList<>();
        for (long i = 1; i <= 11; i++) {
            Theme theme = new Theme(i, "T" + i, "T" + i, "T" + i);
            for (int j = 0; j < i; j++) {
                reservations.add(new Reservation(null, LocalDate.now(), new ReservationTime(LocalTime.now()), theme,
                        ReservationStatus.RESERVED));
            }
        }

        ThemeRanking ranking = new ThemeRanking(reservations);

        // when
        List<Theme> result = ranking.getAscendingRanking();

        // then
        assertThat(result).hasSize(10);
    }
}

