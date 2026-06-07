package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class ReservationSequenceTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
    private final Slot slot = new Slot(LocalDate.of(2026, 5, 15), time, theme);

    @Test
    @DisplayName("예약 순서의 첫 번째 예약은 예약 확정이고 이후 예약은 대기 순번을 가진다.")
    void entries_success_calculatesStatusAndWaitingRank() {
        Reservation reservedReservation = reservation(1L, "브라운");
        Reservation firstWaitingReservation = reservation(2L, "레아");
        Reservation secondWaitingReservation = reservation(3L, "포비");

        ReservationSequence sequence = new ReservationSequence(
                slot,
                List.of(reservedReservation, firstWaitingReservation, secondWaitingReservation)
        );

        List<ReservationEntry> entries = sequence.entries();

        assertThat(entries)
                .extracting(
                        entry -> entry.reservation().getId(),
                        ReservationEntry::status,
                        ReservationEntry::waitingRank
                )
                .containsExactly(
                        tuple(1L, ReservationStatus.RESERVED, 0L),
                        tuple(2L, ReservationStatus.WAITING, 1L),
                        tuple(3L, ReservationStatus.WAITING, 2L)
                );
    }

    @Test
    @DisplayName("서로 다른 슬롯의 예약은 하나의 예약 순서로 묶을 수 없다.")
    void create_fail_whenReservationsHaveDifferentSlot() {
        Reservation reservation = reservation(1L, "브라운");
        Reservation otherSlotReservation = new Reservation(
                2L,
                "레아",
                new Slot(LocalDate.of(2026, 5, 16), time, theme)
        );

        assertThatThrownBy(() -> new ReservationSequence(slot, List.of(reservation, otherSlotReservation)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Reservation reservation(Long id, String name) {
        return new Reservation(id, name, slot);
    }
}
