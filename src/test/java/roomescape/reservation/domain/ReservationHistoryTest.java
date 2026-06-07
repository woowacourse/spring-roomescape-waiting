package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationHistoryTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");
    private final Slot slot = new Slot(LocalDate.of(2026, 5, 15), time, theme);

    @Test
    @DisplayName("예약 이력은 취소 상태의 예약 조회 모델로 변환된다.")
    void canceled_success_createsCanceledReservationEntry() {
        ReservationHistory history = new ReservationHistory(
                1L,
                "브라운",
                slot,
                1L,
                LocalDateTime.of(2026, 5, 1, 10, 0),
                LocalDateTime.of(2026, 5, 2, 10, 0)
        );

        ReservationEntry entry = history.canceled();

        assertThat(entry.status()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(entry.waitingRank()).isNull();
        assertThat(entry.reservation().getId()).isEqualTo(1L);
        assertThat(entry.reservation().getName()).isEqualTo("브라운");
        assertThat(entry.reservation().getSlot()).isEqualTo(slot);
    }
}
