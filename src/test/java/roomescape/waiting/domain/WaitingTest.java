package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class WaitingTest {

    @Test
    @DisplayName("예약 대기를 생성한다")
    void create_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "무서운 테마", "https://good.com/thumb-nail/1");
        LocalDate date = LocalDate.now().plusDays(1);

        // when
        Waiting waiting = Waiting.create("브라운", date, time, theme);

        // then
        assertThat(waiting.getId()).isNull();
        assertThat(waiting.getName()).isEqualTo("브라운");
        assertThat(waiting.getDate()).isEqualTo(date);
        assertThat(waiting.getTime()).isEqualTo(time);
        assertThat(waiting.getTheme()).isEqualTo(theme);
        assertThat(waiting.getRank()).isNull();
        assertThat(waiting.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("지난 일정으로 예약 대기를 생성하면 예외가 발생한다")
    void create_fail_with_past_schedule() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "무서운 테마", "https://good.com/thumb-nail/1");
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // when & then
        assertThatThrownBy(() -> Waiting.create("브라운", pastDate, time, theme))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("지난 일정으로 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("생성된 예약 대기에 ID를 부여한다")
    void appendId_success() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "무서운 테마", "https://good.com/thumb-nail/1");
        Waiting waiting = Waiting.create("브라운", LocalDate.now().plusDays(1), time, theme);

        // when
        Waiting savedWaiting = waiting.appendId(1L);

        // then
        assertThat(savedWaiting.getId()).isEqualTo(1L);
        assertThat(savedWaiting.getName()).isEqualTo(waiting.getName());
        assertThat(savedWaiting.getDate()).isEqualTo(waiting.getDate());
        assertThat(savedWaiting.getTime()).isEqualTo(waiting.getTime());
        assertThat(savedWaiting.getTheme()).isEqualTo(waiting.getTheme());
        assertThat(savedWaiting.getRank()).isEqualTo(waiting.getRank());
    }
}
