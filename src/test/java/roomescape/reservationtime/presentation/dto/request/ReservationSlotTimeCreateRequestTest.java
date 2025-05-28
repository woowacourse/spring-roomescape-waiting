package roomescape.reservationtime.presentation.dto.request;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReservationSlotTimeCreateRequestTest {

    @Test
    void create_shouldThrowException_whenStartAtNull() {
        assertThatThrownBy(
                () -> new ReservationTimeCreateWebRequest(null)
        ).hasMessage("시작 시간은 null일 수 없습니다.");
    }
}
