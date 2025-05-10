package roomescape.reservation.dto.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidInputException;

public class ReservationCreateRequestTest {

    @Test
    void 날짜는_빈_값이_들어올_수_없다() {
        assertThatThrownBy(() -> new ReservationCreateRequest(null, 1L, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 날짜가 입력되지 않았다.");
    }

    @Test
    void 시간은_빈_값이_들어올_수_없다() {
        assertThatThrownBy(() -> new ReservationCreateRequest(LocalDate.of(2025, 4, 29), null, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 시간이 입력되지 않았다.");
    }

    @Test
    void 테마는_빈_값이_들어올_수_없다() {
        assertThatThrownBy(() -> new ReservationCreateRequest(LocalDate.of(2025, 4, 29), 1L, null))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 테마가 입력되지 않았다.");
    }
}
