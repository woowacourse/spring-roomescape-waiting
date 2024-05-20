package roomescape.core.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaitingTest {
    private static final Member member = new Member("리건", "test@email.com", "password", Role.ADMIN);
    private static final Theme theme = new Theme("테마", "테마 설명", "테마 이미지");
    private static final ReservationTime time = new ReservationTime("10:00");

    @Test
    @DisplayName("예약 대기 생성 시, 잘못된 예약 대기 날짜 형식이면 예외가 발생한다.")
    void validateDateFormat() {
        final String date = "2222222222";

        assertThatThrownBy(() -> new Waiting(member, date, time, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("날짜 형식이 잘못되었습니다.");
    }
}
