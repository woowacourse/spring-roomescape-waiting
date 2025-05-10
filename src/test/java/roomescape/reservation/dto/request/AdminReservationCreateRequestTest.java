package roomescape.reservation.dto.request;

import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidInputException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AdminReservationCreateRequestTest {

    @Test
    void 날짜는_빈_값이_들어올_수_없다() {
        assertThatThrownBy(() -> new AdminReservationCreateRequest(null, 1L, 1L, 1L))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("예약할 날짜가 입력되지 않았다.");
    }

    @Test
    void 멤버는_빈_값이_들어올_수_없다() {
        assertThatThrownBy(() -> new AdminReservationCreateRequest(LocalDate.of(2025, 4, 29), null, 1L, 1L))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("예약하는 멤버가 누구인지 입력되지 않았다.");
    }

    @Test
    void 시간은_빈_값이_들어올_수_없다() {
        assertThatThrownBy(() -> new AdminReservationCreateRequest(LocalDate.of(2025, 4, 29), 1L, null, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 시간이 입력되지 않았다.");
    }

    @Test
    void 테마는_빈_값이_들어올_수_없다() {
        assertThatThrownBy(() -> new AdminReservationCreateRequest(LocalDate.of(2025, 4, 29), 1L, 1L, null))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 테마가 입력되지 않았다.");
    }
}
