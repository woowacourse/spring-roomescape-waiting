package roomescape.slot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlotOccupancyTest {

    @Test
    @DisplayName("예약과 대기가 없으면 예약할 수 있다.")
    void reservableWhenEmpty() {
        SlotOccupancy occupancy = SlotOccupancy.of(false, false);

        assertThat(occupancy.isReservable()).isTrue();
    }

    @Test
    @DisplayName("예약이 있으면 예약할 수 없다.")
    void notReservableWhenReserved() {
        SlotOccupancy occupancy = SlotOccupancy.of(true, false);

        assertThat(occupancy.isReservable()).isFalse();
    }

    @Test
    @DisplayName("대기가 있으면 예약할 수 없다.")
    void notReservableWhenWaitingExists() {
        SlotOccupancy occupancy = SlotOccupancy.of(false, true);

        assertThat(occupancy.isReservable()).isFalse();
    }

    @Test
    @DisplayName("예약이 있으면 대기할 수 있다.")
    void waitableWhenReserved() {
        SlotOccupancy occupancy = SlotOccupancy.of(true, false);

        assertThat(occupancy.isWaitable()).isTrue();
    }

    @Test
    @DisplayName("대기가 있으면 대기할 수 있다.")
    void waitableWhenWaitingExists() {
        SlotOccupancy occupancy = SlotOccupancy.of(false, true);

        assertThat(occupancy.isWaitable()).isTrue();
    }

    @Test
    @DisplayName("예약과 대기가 없으면 대기할 수 없다.")
    void notWaitableWhenEmpty() {
        SlotOccupancy occupancy = SlotOccupancy.of(false, false);

        assertThat(occupancy.isWaitable()).isFalse();
    }
}
