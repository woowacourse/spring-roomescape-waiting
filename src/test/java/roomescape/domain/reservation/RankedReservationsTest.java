package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.RoomEscapeFixture;

class RankedReservationsTest {
    @Test
    void 같은_슬롯에서_먼저_예약한_사람이_rank_0이다() {
        Reservation first = RoomEscapeFixture.reservation()
                .name("제제").createdAt(LocalDateTime.of(2099, 1, 1, 9, 0)).build();
        Reservation second = RoomEscapeFixture.reservation()
                .id(2L).name("달수").status(Status.WAITING).createdAt(LocalDateTime.of(2099, 1, 1, 9, 1)).build();

        RankedReservations rankedReservations = new RankedReservations(List.of(first, second));
        List<RankedReservation> results = rankedReservations.resultsOf();

        assertThat(results.get(0).getRank().getValue()).isEqualTo(0);
        assertThat(results.get(1).getRank().getValue()).isEqualTo(1);
    }

    @Test
    void 이름으로_조회하면_해당_이름의_예약만_반환된다() {
        Reservation r1 = RoomEscapeFixture.reservation()
                .name("제제").createdAt(LocalDateTime.of(2099, 1, 1, 9, 0)).build();
        Reservation r2 = RoomEscapeFixture.reservation()
                .id(2L).name("달수").status(Status.WAITING).createdAt(LocalDateTime.of(2099, 1, 1, 9, 1)).build();

        RankedReservations rankedReservations = new RankedReservations(List.of(r1, r2));
        List<RankedReservation> results = rankedReservations.resultsOf("달수");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getReservation()).isEqualTo(r2);
    }
}
