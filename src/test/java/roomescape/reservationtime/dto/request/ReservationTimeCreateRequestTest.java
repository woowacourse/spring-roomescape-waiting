package roomescape.reservationtime.dto.request;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReservationTimeCreateRequestTest {

    @Test
    void create_shouldThrowException_whenStartAtNull() {
        assertThatThrownBy(
                () -> new ReservationTimeCreateRequest(null)
        ).hasMessage("시작 시간은 null일 수 없습니다.");
    }
}
