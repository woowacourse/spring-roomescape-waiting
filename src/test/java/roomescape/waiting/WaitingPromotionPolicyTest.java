package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.Theme;

class WaitingPromotionPolicyTest {

    private final WaitingPromotionPolicy policy = new WaitingPromotionPolicy();

    @Test
    @DisplayName("대기를 같은 슬롯의 예약으로 전환한다.")
    void promote_success() {
        Waiting waiting = Waiting.of(1L, 2L, 10L);
        Slot slot = slot(10L);

        Reservation reservation = policy.promote(waiting, slot);

        assertThat(reservation.getId()).isNull();
        assertThat(reservation.getMemberId()).isEqualTo(waiting.getMemberId());
        assertThat(reservation.getSlot()).isEqualTo(slot);
    }

    @Test
    @DisplayName("대기 슬롯과 예약 슬롯이 다르면 전환에 실패한다.")
    void promote_different_slot_fail() {
        Waiting waiting = Waiting.of(1L, 2L, 10L);
        Slot otherSlot = slot(20L);

        assertThatThrownBy(() -> policy.promote(waiting, otherSlot))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Slot slot(long slotId) {
        return Slot.of(
                slotId,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "theme", "description", "thumbnail")
        );
    }
}
