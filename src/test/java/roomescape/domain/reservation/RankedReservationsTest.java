package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

class RankedReservationsTest {
    private static final Slot SLOT = Slot.load(1L,
            new ReservationDate(LocalDate.of(2099, 1, 1)),
            ReservationTime.of(1L, LocalTime.of(10, 0)),
            Theme.load(1L, new ThemeName("테마"), "설명", new ThumbnailUrl("https://zeze.com")));

    @Test
    void 같은_슬롯에서_먼저_예약한_사람이_rank_0이다() {
        Reservation first = Reservation.load(1L, new ReservationName("제제"), SLOT, Status.APPROVED,
                LocalDateTime.of(2099, 1, 1, 9, 0));
        Reservation second = Reservation.load(2L, new ReservationName("달수"), SLOT, Status.WAITING,
                LocalDateTime.of(2099, 1, 1, 9, 1));

        RankedReservations rankedReservations = new RankedReservations(List.of(first, second));

        List<RankedReservation> results = rankedReservations.resultsOf();

        assertThat(results.get(0).getRank().getValue()).isEqualTo(0);
        assertThat(results.get(1).getRank().getValue()).isEqualTo(1);
    }

    @Test
    void 이름으로_조회하면_해당_이름의_예약만_반환된다() {
        Reservation r1 = Reservation.load(1L, new ReservationName("제제"), SLOT, Status.APPROVED,
                LocalDateTime.of(2099, 1, 1, 9, 0));
        Reservation r2 = Reservation.load(2L, new ReservationName("달수"), SLOT, Status.WAITING,
                LocalDateTime.of(2099, 1, 1, 9, 1));

        RankedReservations rankedReservations = new RankedReservations(List.of(r1, r2));

        List<RankedReservation> results = rankedReservations.resultsOf("달수");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getReservation()).isEqualTo(r2);
    }
}
