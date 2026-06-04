package roomescape.waiting;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.ReservationTime;
import roomescape.slot.Slot;
import roomescape.theme.Theme;

public class WaitingTest {

    @Test
    @DisplayName("대기자가 같으면 본인 대기다.")
    void 대기_테스트_1() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);

        assertThat(waiting.isOwnedBy(1L)).isTrue();
    }

    @Test
    @DisplayName("대기자가 다르면 본인 대기가 아니다.")
    void 대기_테스트_2() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);

        assertThat(waiting.isOwnedBy(2L)).isFalse();
    }

    @Test
    @DisplayName("대기자가 다르면 소유자 검증에 실패한다.")
    void validateOwnedBy_fail() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);

        assertThatThrownBy(() -> waiting.validateOwnedBy(2L))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("대기가 속한 슬롯과 전달받은 슬롯이 같으면 true를 반환한다.")
    void isFor_true() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);

        assertThat(waiting.isFor(slot(1L))).isTrue();
    }

    @Test
    @DisplayName("대기가 속한 슬롯과 전달받은 슬롯이 다르면 false를 반환한다.")
    void isFor_false() {
        Waiting waiting = Waiting.of(1L, 1L, 1L);

        assertThat(waiting.isFor(slot(2L))).isFalse();
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
