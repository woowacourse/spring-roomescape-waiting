package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;

class WaitingTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 5, 12, 0);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 6, 1, 9, 0);

    private Slot slot(LocalDate date, LocalTime startAt) {
        return new Slot(date, ReservationTime.create(1, startAt), Theme.create(1, "테마", "url", "설명"));
    }

    private Waiting waiting(String name, Slot slot) {
        return Waiting.create(1, name, slot, CREATED_AT);
    }

    @Test
    @DisplayName("타인의 예약대기면 소유권 검증에서 예외를 던진다.")
    void validateOwnedByThrows() {
        Waiting waiting = waiting("me", slot(LocalDate.of(2026, 6, 6), LocalTime.of(10, 0)));

        assertThatThrownBy(() -> waiting.validateOwnedBy("other"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("본인의 예약대기면 소유권 검증을 통과한다.")
    void validateOwnedByOk() {
        Waiting waiting = waiting("me", slot(LocalDate.of(2026, 6, 6), LocalTime.of(10, 0)));

        assertThatCode(() -> waiting.validateOwnedBy("me"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 시작된 예약대기는 검증 시 예외를 던진다.")
    void validateNotStartedThrows() {
        Waiting waiting = waiting("me", slot(LocalDate.of(2026, 6, 5), LocalTime.of(10, 0)));

        assertThatThrownBy(() -> waiting.validateNotStarted(NOW))
                .isInstanceOf(PastReservationException.class);
    }

    @Test
    @DisplayName("같은 슬롯이고 생성 시각이 같으면 id가 작은 쪽이 앞선다.")
    void isAheadOfByIdTieBreak() {
        Slot slot = slot(LocalDate.of(2026, 6, 6), LocalTime.of(10, 0));
        Waiting smaller = Waiting.create(1, "a", slot, CREATED_AT);
        Waiting bigger = Waiting.create(2, "b", slot, CREATED_AT);

        assertThat(smaller.isAheadOf(bigger)).isTrue();
        assertThat(bigger.isAheadOf(smaller)).isFalse();
    }
}
