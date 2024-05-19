package roomescape.service.mapper;

import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;
import static roomescape.fixture.ReservationFixture.DEFAULT_RESPONSE;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.ReservationResponse;

class ReservationResponseMapperTest {

    @Test
    @DisplayName("도메인을 응답으로 잘 변환하는지 확인")
    void toResponse() {
        ReservationResponse response = ReservationResponseMapper.toResponse(DEFAULT_RESERVATION);

        Assertions.assertThat(response)
                .isEqualTo(DEFAULT_RESPONSE);
    }
}
