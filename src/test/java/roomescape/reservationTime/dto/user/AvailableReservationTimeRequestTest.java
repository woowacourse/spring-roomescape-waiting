package roomescape.reservationTime.dto.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidDateException;
import roomescape.common.exception.InvalidIdException;

class AvailableReservationTimeRequestTest {

    @DisplayName("날짜가 널 값일 경우 예외가 발생한다.")
    @Test
    void exception_date_null() {
        assertThatThrownBy(() -> new AvailableReservationTimeRequest(null, 1L))
                .isInstanceOf(InvalidDateException.class)
                .hasMessage("날짜를 입력해주세요");
    }

    @DisplayName("날짜가 현재보다 이전일 경우 예외가 발생한다.")
    @Test
    void exception_date_before() {
        assertThatThrownBy(() -> new AvailableReservationTimeRequest(LocalDate.now().minusDays(1), 1L))
                .isInstanceOf(InvalidDateException.class)
                .hasMessage("이미 지난 날짜로는 예약할 수 없습니다.");
    }

    @DisplayName("테마 아이디가 널 값인 경우 예외가 발생한다.")
    @Test
    void exception_themeId_null() {
        assertThatThrownBy(() -> new AvailableReservationTimeRequest(LocalDate.now(), null))
                .isInstanceOf(InvalidIdException.class)
                .hasMessage("테마 아이디를 입력해주세요");
    }

    @DisplayName("유효한 날짜와 테마 아이디로 객체가 생성된다.")
    @Test
    void create_valid_request() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        AvailableReservationTimeRequest request = new AvailableReservationTimeRequest(tomorrow, 1L);

        assertThat(request.date()).isEqualTo(tomorrow);
    }
}
