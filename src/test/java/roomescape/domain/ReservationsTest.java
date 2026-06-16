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

class ReservationsTest {

    private static final Long THEME_SLOT_ID = 1L;
    private static final Theme THEME = new Theme(1L, "테마", "설명", "test.com", 10000L);
    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final Time TIME = new Time(1L, LocalTime.of(10, 0));

    private Reservation pending(Long id, String name) {
        return new Reservation(id, name, THEME_SLOT_ID, DATE, TIME, THEME, PendingStatus.getInstance());
    }

    private Reservation confirmed(Long id, String name) {
        return new Reservation(id, name, THEME_SLOT_ID, DATE, TIME, THEME, ConfirmedStatus.getInstance());
    }

    private Reservation cancelled(Long id, String name) {
        return new Reservation(id, name, THEME_SLOT_ID, DATE, TIME, THEME, CancelledStatus.getInstance());
    }

    @Test
    @DisplayName("같은 이름의 활성 예약이 있으면 중복 예외가 발생한다.")
    void validateDuplicate_throwsWhenActiveDuplicateExists() {
        Reservations reservations = new Reservations(List.of(confirmed(1L, "브라운")));
        assertThatThrownBy(() -> reservations.validateDuplicate("브라운"))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 같은 시간에 예약 또는 대기를 신청했습니다.");
    }

    @Test
    @DisplayName("같은 이름이라도 취소된 예약만 있으면 중복 예외가 발생하지 않는다.")
    void validateDuplicate_doesNotThrowWhenOnlyCancelledExists() {
        Reservations reservations = new Reservations(List.of(cancelled(1L, "브라운")));
        reservations.validateDuplicate("브라운"); // 예외 없음
    }

    @Test
    @DisplayName("예약 목록이 비어 있으면 중복 예외가 발생하지 않는다.")
    void validateDuplicate_doesNotThrowWhenEmpty() {
        Reservations reservations = new Reservations(List.of());
        reservations.validateDuplicate("브라운"); // 예외 없음
    }

    @Test
    @DisplayName("예약 목록이 비어 있으면 활성 예약이 없다고 판단한다.")
    void hasNoActiveReservation_trueWhenEmpty() {
        Reservations reservations = new Reservations(List.of());
        assertThat(reservations.hasNoActiveReservation()).isTrue();
    }

    @Test
    @DisplayName("모든 예약이 취소되면 활성 예약이 없다고 판단한다.")
    void hasNoActiveReservation_trueWhenAllCancelled() {
        Reservations reservations = new Reservations(List.of(
                cancelled(1L, "브라운"),
                cancelled(2L, "포비")
        ));
        assertThat(reservations.hasNoActiveReservation()).isTrue();
    }

    @Test
    @DisplayName("CONFIRMED 예약이 있으면 활성 예약이 있다고 판단한다.")
    void hasNoActiveReservation_falseWhenConfirmedExists() {
        Reservations reservations = new Reservations(List.of(confirmed(1L, "브라운")));
        assertThat(reservations.hasNoActiveReservation()).isFalse();
    }

    @Test
    @DisplayName("PENDING 예약이 있으면 활성 예약이 있다고 판단한다.")
    void hasNoActiveReservation_falseWhenPendingExists() {
        Reservations reservations = new Reservations(List.of(
                confirmed(1L, "브라운"),
                pending(2L, "포비")
        ));
        assertThat(reservations.hasNoActiveReservation()).isFalse();
    }

    @Test
    @DisplayName("PENDING 예약 중 ID가 가장 작은 것을 반환한다.")
    void findFirstPending_returnsLowestIdAmongPending() {
        Reservations reservations = new Reservations(List.of(
                confirmed(1L, "브라운"),
                pending(3L, "포비"),
                pending(2L, "네오")
        ));
        Optional<Reservation> first = reservations.findFirstPending();
        assertThat(first).isPresent();
        assertThat(first.get().getName()).isEqualTo("네오");
    }

    @Test
    @DisplayName("PENDING 예약이 없으면 빈 Optional을 반환한다.")
    void findFirstPending_emptyWhenNoPendingExists() {
        Reservations reservations = new Reservations(List.of(confirmed(1L, "브라운")));
        assertThat(reservations.findFirstPending()).isEmpty();
    }

    @Test
    @DisplayName("대기 순번을 신청 순서(ID 오름차순)대로 부여한다.")
    void waitingOrderOf_returnsCorrectOrderByIdAsc() {
        Reservation pending1 = pending(2L, "네오");
        Reservation pending2 = pending(3L, "포비");
        Reservation pending3 = pending(5L, "제이슨");
        Reservations reservations = new Reservations(List.of(
                confirmed(1L, "브라운"), pending1, pending2, pending3
        ));

        assertThat(reservations.waitingOrderOf(2L)).isEqualTo(1);
        assertThat(reservations.waitingOrderOf(3L)).isEqualTo(2);
        assertThat(reservations.waitingOrderOf(5L)).isEqualTo(3);
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 예약의 순번을 조회하면 예외가 발생한다.")
    void waitingOrderOf_throwsWhenReservationIsNotPending() {
        Reservations reservations = new Reservations(List.of(confirmed(1L, "브라운")));
        assertThatThrownBy(() -> reservations.waitingOrderOf(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PENDING 예약만 ID 오름차순으로 반환한다.")
    void pendingByOrder_returnsOnlyPendingInIdOrder() {
        Reservations reservations = new Reservations(List.of(
                confirmed(1L, "브라운"),
                pending(3L, "포비"),
                cancelled(4L, "제이슨"),
                pending(2L, "네오")
        ));

        List<Reservation> result = reservations.pendingByOrder();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("네오");
        assertThat(result.get(1).getName()).isEqualTo("포비");
    }

    @Test
    @DisplayName("PENDING 예약이 없으면 빈 목록을 반환한다.")
    void pendingByOrder_emptyWhenNoPendingExists() {
        Reservations reservations = new Reservations(List.of(confirmed(1L, "브라운")));
        assertThat(reservations.pendingByOrder()).isEmpty();
    }

    @Test
    @DisplayName("ID로 예약을 조회한다.")
    void findById_returnsCorrectReservation() {
        Reservations reservations = new Reservations(List.of(
                pending(1L, "브라운"),
                pending(2L, "포비")
        ));
        assertThat(reservations.findById(2L).getName()).isEqualTo("포비");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다.")
    void findById_throwsWhenNotFound() {
        Reservations reservations = new Reservations(List.of(pending(1L, "브라운")));
        assertThatThrownBy(() -> reservations.findById(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage("예약이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("예약을 추가하면 목록 크기가 늘어난다.")
    void add_increasesSize() {
        Reservations reservations = new Reservations(List.of(confirmed(1L, "브라운")));
        reservations.add(pending(2L, "포비"));
        assertThat(reservations.toList()).hasSize(2);
    }

    @Test
    @DisplayName("toList()는 외부에서 수정할 수 없는 목록을 반환한다.")
    void toList_returnsUnmodifiableList() {
        Reservations reservations = new Reservations(List.of(confirmed(1L, "브라운")));
        List<Reservation> result = reservations.toList();
        assertThatThrownBy(() -> result.add(pending(2L, "포비")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
