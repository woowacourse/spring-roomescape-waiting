package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;

public class WaitingTest {

    @Test
    @DisplayName("대기자가 같으면 본인 대기다.")
    void same_member_is_owner_of_waiting() {
        Waiting waiting = roomescape.TestFixtures.waiting(1L, 1L, 1L);

        assertThat(waiting.isOwnedBy(1L)).isTrue();
    }

    @Test
    @DisplayName("대기자가 다르면 본인 대기가 아니다.")
    void different_member_is_not_owner_of_waiting() {
        Waiting waiting = roomescape.TestFixtures.waiting(1L, 1L, 1L);

        assertThat(waiting.isOwnedBy(2L)).isFalse();
    }

    @Test
    @DisplayName("대기자가 다르면 소유자 검증에 실패한다.")
    void different_member_fails_waiting_owner_validation() {
        Waiting waiting = roomescape.TestFixtures.waiting(1L, 1L, 1L);

        assertThatThrownBy(() -> waiting.validateOwnedBy(2L))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("대기가 속한 슬롯과 전달받은 슬롯이 같으면 true를 반환한다.")
    void waiting_for_same_slot_returns_true() {
        Waiting waiting = roomescape.TestFixtures.waiting(1L, 1L, 1L);

        assertThat(waiting.isFor(slot(1L))).isTrue();
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
    @DisplayName("대기가 속한 슬롯과 전달받은 슬롯이 다르면 false를 반환한다.")
    void waiting_for_different_slot_returns_false() {
        Waiting waiting = roomescape.TestFixtures.waiting(1L, 1L, 1L);

        assertThat(waiting.isFor(slot(2L))).isFalse();
    }

}
