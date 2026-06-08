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

class ReservationsTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final ReservationTime laterTime = new ReservationTime(2L, LocalTime.of(12, 0));
    private final Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
    private final Slot slot = new Slot(LocalDate.of(2026, 5, 15), time, theme);

    @Test
    @DisplayName("신청 순서의 첫 번째 예약은 예약 확정이고 이후 예약은 대기 순번을 가진다.")
    void entries_success_calculatesStatusAndWaitingRank() {
        Reservation reservedReservation = reservation(1L, "브라운", 1L);
        Reservation firstWaitingReservation = reservation(2L, "레아", 2L);
        Reservation secondWaitingReservation = reservation(3L, "포비", 3L);

        List<ReservationEntry> entries = new Reservations(List.of(
                firstWaitingReservation,
                secondWaitingReservation,
                reservedReservation
        )).entries();

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
    @DisplayName("예약 목록은 날짜와 시간 순서로 정렬하고 같은 슬롯은 신청 순서로 상태를 계산한다.")
    void entries_success_calculatesStatusAndWaitingRankPerSlot() {
        Reservation reservation = reservation(1L, "브라운", 1L);
        Reservation otherSlotReservation = reservation(
                2L,
                "레아",
                new Slot(LocalDate.of(2026, 5, 16), time, theme),
                4L
        );
        Reservation waitingReservation = reservation(3L, "포비", 2L);
        Reservation laterTimeReservation = reservation(
                4L,
                "제이슨",
                new Slot(LocalDate.of(2026, 5, 15), laterTime, theme),
                3L
        );

        List<ReservationEntry> entries = new Reservations(List.of(
                otherSlotReservation,
                waitingReservation,
                laterTimeReservation,
                reservation
        )).entries();

        assertThat(entries)
                .extracting(
                        entry -> entry.reservation().getId(),
                        ReservationEntry::status,
                        ReservationEntry::waitingRank
                )
                .containsExactly(
                        tuple(1L, ReservationStatus.RESERVED, 0L),
                        tuple(3L, ReservationStatus.WAITING, 1L),
                        tuple(4L, ReservationStatus.RESERVED, 0L),
                        tuple(2L, ReservationStatus.RESERVED, 0L)
                );
    }

    @Test
    @DisplayName("신청 순서가 없는 예약 목록은 상태와 대기 순번을 계산할 수 없다.")
    void entries_fail_whenRequestOrderIsNull() {
        Reservation reservation = reservation(1L, "브라운", null);

        assertThatThrownBy(() -> new Reservations(List.of(reservation)).entries())
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Reservation reservation(Long id, String name, Long requestOrder) {
        return Reservation.reconstruct(id, name, slot, requestOrder);
    }

    private Reservation reservation(Long id, String name, Slot slot, Long requestOrder) {
        return Reservation.reconstruct(id, name, slot, requestOrder);
    }
}
