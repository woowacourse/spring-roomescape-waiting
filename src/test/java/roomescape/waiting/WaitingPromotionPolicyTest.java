package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingPromotionPolicy;

class WaitingPromotionPolicyTest {

    private final WaitingPromotionPolicy policy = new WaitingPromotionPolicy();

    @Test
    @DisplayName("대기를 같은 슬롯의 예약으로 전환한다.")
    void promotes_waiting_to_reservation_in_same_slot() {
        Waiting waiting = roomescape.TestFixtures.waiting(1L, 2L, 10L);
        Slot slot = slot(10L);

        Reservation reservation = policy.promote(waiting, slot);

        assertThat(reservation.getId()).isNull();
        assertThat(reservation.getMemberId()).isEqualTo(waiting.getMemberId());
        assertThat(reservation.getSlot()).isEqualTo(slot);
    }

    private Slot slot(long slotId) {
        return Slot.of(
                slotId,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "theme", "description", "thumbnail")
        );
    }

    @Test
    @DisplayName("대기 슬롯과 예약 슬롯이 다르면 전환에 실패한다.")
    void promotion_fails_for_different_waiting_and_reservation_slots() {
        Waiting waiting = roomescape.TestFixtures.waiting(1L, 2L, 10L);
        Slot otherSlot = slot(20L);

        assertThatThrownBy(() -> policy.promote(waiting, otherSlot))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
