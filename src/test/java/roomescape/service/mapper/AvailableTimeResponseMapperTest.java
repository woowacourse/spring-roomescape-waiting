package roomescape.service.mapper;

import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;

import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.AvailableTimeResponse;

class AvailableTimeResponseMapperTest {

    @Test
    @DisplayName("도메인을 응답으로 잘 변환하는지 확인")
    void toResponse() {
        AvailableTimeResponse response = AvailableTimeResponseMapper.toResponse(Set.of(DEFAULT_TIME), DEFAULT_TIME);

        Assertions.assertThat(response)
                .isEqualTo(new AvailableTimeResponse(DEFAULT_TIME.getId(), DEFAULT_TIME.getStartAt(), true));
    }
}
