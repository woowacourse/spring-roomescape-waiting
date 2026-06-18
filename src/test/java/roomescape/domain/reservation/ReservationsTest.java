package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.RoomEscapeFixture;

class ReservationsTest {
    private static final Slot SLOT = RoomEscapeFixture.slot().build();
    private static final ReservationName NAME = new ReservationName("zeze");
    private static final LocalDateTime NOW = RoomEscapeFixture.PAST_DATE_TIME;

    @Test
    void null로_생성되면_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> new Reservations(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void 예약이_없으면_APPROVED로_생성된다() {
        Reservations reservations = new Reservations(List.of());

        Reservation result = reservations.reserve(NAME, SLOT, NOW);

        assertThat(result.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void APPROVED_예약이_있으면_WAITING으로_생성된다() {
        Reservation existing = RoomEscapeFixture.reservation().status(Status.APPROVED).build();
        Reservations reservations = new Reservations(List.of(existing));

        Reservation result = reservations.reserve(new ReservationName("달수"), SLOT, NOW);

        assertThat(result.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    void 같은_슬롯에_같은_이름이_있으면_예외가_발생한다() {
        Reservation existing = RoomEscapeFixture.reservation().build();
        Reservations reservations = new Reservations(List.of(existing));

        assertThatThrownBy(() -> reservations.reserve(NAME, SLOT, NOW))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 같은_슬롯에_다른_이름이면_예약이_가능하다() {
        Reservation existing = RoomEscapeFixture.reservation().name("달수").build();
        Reservations reservations = new Reservations(List.of(existing));

        Reservation result = reservations.reserve(NAME, SLOT, NOW);

        assertThat(result.getName()).isEqualTo(NAME);
    }

    @Test
    void 다른_슬롯에_같은_이름이면_예약이_가능하다() {
        Slot otherSlot = RoomEscapeFixture.slot().id(2L).build();
        Reservation existing = RoomEscapeFixture.reservation().slot(otherSlot).build();
        Reservations reservations = new Reservations(List.of(existing));

        Reservation result = reservations.reserve(NAME, SLOT, NOW);

        assertThat(result.getName()).isEqualTo(NAME);
    }

    @Test
    void 같은_슬롯에서_먼저_예약한_사람이_rank_0이다() {
        Reservation first = RoomEscapeFixture.reservation()
                .name("제제").createdAt(LocalDateTime.of(2099, 1, 1, 9, 0)).build();
        Reservation second = RoomEscapeFixture.reservation()
                .id(2L).name("달수").status(Status.WAITING).createdAt(LocalDateTime.of(2099, 1, 1, 9, 1)).build();

        Reservations rankedReservations = new Reservations(List.of(first, second));
        List<RankedReservation> results = rankedReservations.rankedReservationsOf();

        assertThat(results.get(0).getRank().getValue()).isEqualTo(0);
        assertThat(results.get(1).getRank().getValue()).isEqualTo(1);
    }

    @Test
    void 이름으로_조회하면_해당_이름의_예약만_반환된다() {
        Reservation r1 = RoomEscapeFixture.reservation()
                .name("제제").createdAt(LocalDateTime.of(2099, 1, 1, 9, 0)).build();
        Reservation r2 = RoomEscapeFixture.reservation()
                .id(2L).name("달수").status(Status.WAITING).createdAt(LocalDateTime.of(2099, 1, 1, 9, 1)).build();

        Reservations rankedReservations = new Reservations(List.of(r1, r2));
        List<RankedReservation> results = rankedReservations.rankedReservationsOf("달수");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getReservation()).isEqualTo(r2);
    }
}
