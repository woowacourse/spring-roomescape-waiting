package roomescape.service.mapper;

import static roomescape.fixture.ReservationWaitingFixture.DEFAULT_RESPONSE;
import static roomescape.fixture.ReservationWaitingFixture.DEFAULT_WAITING;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.ReservationWaitingResponse;

class ReservationWaitingResponseMapperTest {

    @Test
    @DisplayName("도메인을 응답으로 잘 변환하는지 확인")
    void toResponse() {
        ReservationWaitingResponse response = ReservationWaitingResponseMapper.toResponse(DEFAULT_WAITING,
                List.of(DEFAULT_WAITING));

        Assertions.assertThat(response)
                .isEqualTo(DEFAULT_RESPONSE);
    }
}
