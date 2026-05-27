package roomescape.domain.reservation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.RoomEscapeFixture;

import static org.junit.jupiter.api.Assertions.*;

class ReservationResultTest {

    @Test
    void 랭크가_1이면_승인을_반환한다() {
        Reservation reservation = RoomEscapeFixture.reservation();
        Rank rank = new Rank(1);
        ReservationResult result = new ReservationResult(rank, reservation);

        Assertions.assertThat(result.status()).isEqualTo("승인");
    }

    @Test
    void 랭크가_1이_아니면_대기를_반환한다() {
        Reservation reservation = RoomEscapeFixture.reservation();
        Rank rank = new Rank(2);

        ReservationResult result = new ReservationResult(rank, reservation);

        Assertions.assertThat(result.status()).isEqualTo("대기");
    }
}