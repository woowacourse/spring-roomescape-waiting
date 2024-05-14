package roomescape.service.mapper;

import static roomescape.fixture.ReservationTimeFixture.DEFAULT_RESPONSE;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.ReservationTimeResponse;

class ReservationTimeResponseMapperTest {

    @Test
    @DisplayName("도메인을 응답으로 잘 변환하는지 확인")
    void toResponse() {
        ReservationTimeResponse response = ReservationTimeResponseMapper.toResponse(DEFAULT_TIME);

        Assertions.assertThat(response)
                .isEqualTo(DEFAULT_RESPONSE);
    }
}
