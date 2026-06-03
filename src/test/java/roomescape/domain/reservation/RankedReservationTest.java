package roomescape.domain.reservation;

import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.RoomEscapeFixture;

class RankedReservationTest {

    private static final Reservation TODAY = RoomEscapeFixture.reservation()
            .createdAt(LocalDateTime.of(2026, 6, 2, 10, 0)).build();

    @Test
    void 예약과_예약목록으로_순번을_정하여_생성된다() {
        List<Reservation> reservations = List.of(
                RoomEscapeFixture.reservation().createdAt(LocalDateTime.of(2026, 6, 1, 10, 0)).build(),
                RoomEscapeFixture.reservation().createdAt(LocalDateTime.of(2026, 6, 1, 10, 1)).build(),
                TODAY,
                RoomEscapeFixture.reservation().createdAt(LocalDateTime.of(2026, 6, 3, 10, 0)).build(),
                RoomEscapeFixture.reservation().createdAt(LocalDateTime.of(2027, 7, 2, 10, 0)).build());

        Assertions.assertThat(RankedReservation.decideRankFrom(TODAY, reservations).getRank().getValue()).isEqualTo(2);
    }
}
