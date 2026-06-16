package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationStatus.CancelledStatus;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.global.exception.CustomException;

class ThemeSlotTest {

    private static final Long SLOT_ID = 1L;
    private static final Theme THEME = new Theme(1L, "테마", "설명", "test.com", 10000L);
    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final Time TIME = new Time(1L, LocalTime.of(10, 0));

    private ThemeSlot emptySlot() {
        return new ThemeSlot(SLOT_ID, THEME, DATE, TIME, false);
    }

    private ThemeSlot slotWith(boolean isReserved, Reservation... reservations) {
        return new ThemeSlot(SLOT_ID, THEME, DATE, TIME, isReserved, List.of(reservations));
    }

    private Reservation pending(Long id, String name) {
        return new Reservation(id, name, SLOT_ID, DATE, TIME, THEME, PendingStatus.getInstance());
    }

    private Reservation confirmed(Long id, String name) {
        return new Reservation(id, name, SLOT_ID, DATE, TIME, THEME, ConfirmedStatus.getInstance());
    }

    private Reservation cancelled(Long id, String name) {
        return new Reservation(id, name, SLOT_ID, DATE, TIME, THEME, CancelledStatus.getInstance());
    }

    @Test
    @DisplayName("빈 슬롯에 첫 예약을 하면 PENDING 상태로 생성되고 슬롯 예약 상태는 변하지 않는다.")
    void addReservation_firstReservation_isPendingAndSlotNotReserved() {
        ThemeSlot themeSlot = emptySlot();

        Reservation reservation = themeSlot.addReservation("브라운");

        assertThat(reservation.isPending()).isTrue();
        assertThat(themeSlot.isReserved()).isFalse();
    }

    @Test
    @DisplayName("같은 슬롯에 두 번째 예약을 해도 PENDING 상태로 생성되고 슬롯 예약 상태는 변하지 않는다.")
    void addReservation_secondReservation_isPending() {
        ThemeSlot themeSlot = emptySlot();
        themeSlot.addReservation("브라운");

        Reservation pending = themeSlot.addReservation("포비");

        assertThat(pending.isPending()).isTrue();
        assertThat(themeSlot.isReserved()).isFalse();
    }

    @Test
    @DisplayName("같은 이름으로 중복 예약하면 예외가 발생한다.")
    void addReservation_duplicateName_throwsException() {
        ThemeSlot themeSlot = emptySlot();
        themeSlot.addReservation("브라운");

        assertThatThrownBy(() -> themeSlot.addReservation("브라운"))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 같은 시간에 예약 또는 대기를 신청했습니다.");
    }

    @Test
    @DisplayName("취소된 예약이 있는 슬롯에 같은 이름으로 다시 예약할 수 있으며 PENDING 상태로 생성된다.")
    void addReservation_afterCancellation_canReserveAgainAsPending() {
        ThemeSlot themeSlot = slotWith(false, cancelled(1L, "브라운"));

        Reservation reservation = themeSlot.addReservation("브라운");

        assertThat(reservation.isPending()).isTrue();
    }

    @Test
    @DisplayName("PENDING 예약을 취소하면 CANCELLED 상태가 되고 슬롯 예약 상태는 변하지 않는다.")
    void cancelReservation_pending_justCancels_slotRemainsReserved() {
        Reservation confirmedReservation = confirmed(1L, "브라운");
        Reservation pendingReservation = pending(2L, "포비");
        ThemeSlot themeSlot = slotWith(true, confirmedReservation, pendingReservation);

        Optional<Reservation> promoted = themeSlot.cancelReservation(2L);

        assertThat(pendingReservation.isCancelled()).isTrue();
        assertThat(promoted).isEmpty();
        assertThat(themeSlot.isReserved()).isTrue();
    }

    @Test
    @DisplayName("CONFIRMED 예약 취소 시 대기자가 있으면 첫 번째 대기자가 CONFIRMED로 승격된다.")
    void cancelReservation_confirmed_withWaiting_promotesFirstPending() {
        Reservation confirmedReservation = confirmed(1L, "브라운");
        Reservation firstWaiting = pending(2L, "네오");
        Reservation secondWaiting = pending(3L, "포비");
        ThemeSlot themeSlot = slotWith(true, confirmedReservation, firstWaiting, secondWaiting);

        Optional<Reservation> promoted = themeSlot.cancelReservation(1L);

        assertThat(confirmedReservation.isCancelled()).isTrue();
        assertThat(promoted).isPresent();
        assertThat(promoted.get().getName()).isEqualTo("네오");
        assertThat(firstWaiting.isConfirmed()).isTrue();
        assertThat(secondWaiting.isPending()).isTrue();
        assertThat(themeSlot.isReserved()).isTrue();
    }

    @Test
    @DisplayName("CONFIRMED 예약 취소 시 대기자가 없으면 슬롯이 예약 해제된다.")
    void cancelReservation_confirmed_noWaiting_setsSlotNotReserved() {
        Reservation confirmedReservation = confirmed(1L, "브라운");
        ThemeSlot themeSlot = slotWith(true, confirmedReservation);

        Optional<Reservation> promoted = themeSlot.cancelReservation(1L);

        assertThat(confirmedReservation.isCancelled()).isTrue();
        assertThat(promoted).isEmpty();
        assertThat(themeSlot.isReserved()).isFalse();
    }

    @Test
    @DisplayName("CONFIRMED 예약 취소 시 여러 대기자 중 ID가 가장 작은 대기자가 승격된다.")
    void cancelReservation_confirmed_multipleWaiting_promotesLowestId() {
        Reservation confirmedReservation = confirmed(1L, "브라운");
        Reservation laterWaiting = pending(5L, "포비");
        Reservation earlierWaiting = pending(3L, "네오");
        ThemeSlot themeSlot = slotWith(true, confirmedReservation, laterWaiting, earlierWaiting);

        themeSlot.cancelReservation(1L);

        assertThat(earlierWaiting.isConfirmed()).isTrue();
        assertThat(laterWaiting.isPending()).isTrue();
    }

    @Test
    @DisplayName("ID로 슬롯 내 예약을 조회한다.")
    void findReservationById_returnsCorrectReservation() {
        Reservation reservation = pending(2L, "포비");
        ThemeSlot themeSlot = slotWith(true, confirmed(1L, "브라운"), reservation);

        assertThat(themeSlot.findReservationById(2L).getName()).isEqualTo("포비");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다.")
    void findReservationById_throwsWhenNotFound() {
        ThemeSlot themeSlot = slotWith(true, confirmed(1L, "브라운"));

        assertThatThrownBy(() -> themeSlot.findReservationById(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage("예약이 존재하지 않습니다.");
    }
}
