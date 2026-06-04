package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingTest {

    @Test
    @DisplayName("예약 대기자 이름이 null이거나 비어있으면 예외가 발생한다.")
    void 예약_대기자_이름_공백_예외_발생() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

        assertThatThrownBy(() -> new Waiting(1L, " ", LocalDate.now().plusDays(1), timeSlot, theme,
                LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 대기자의 이름은 필수입니다.");
    }

    @Test
    @DisplayName("예약 대기 날짜가 null이면 예외가 발생한다.")
    void 예약_대기_날짜_null_예외_발생() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

        assertThatThrownBy(() -> new Waiting(1L, "브라운", null, timeSlot, theme, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 대기 날짜는 필수입니다.");
    }

    @Test
    @DisplayName("예약 대기 시간 객체가 null이면 예외가 발생한다.")
    void 예약_대기_시간_null_예외_발생() {
        Theme theme = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

        assertThatThrownBy(() -> new Waiting(1L, "브라운", LocalDate.now().plusDays(1), null, theme,
                LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 대기 시간은 필수입니다.");
    }

    @Test
    @DisplayName("예약 대기 테마가 null이면 예외가 발생한다.")
    void 예약_대기_테마_null_예외_발생() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));

        assertThatThrownBy(() -> new Waiting(1L, "브라운", LocalDate.now().plusDays(1), timeSlot, null,
                LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마는 필수입니다.");
    }

    @Test
    @DisplayName("예약 대기 생성 시각이 null이면 예외가 발생한다.")
    void 예약_대기_생성_시각_null_예외_발생() {
        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

        assertThatThrownBy(() -> new Waiting(1L, "브라운", LocalDate.now().plusDays(1), timeSlot, theme, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 대기 생성 시각은 필수입니다.");
    }
}
