package roomescape.service.mapper;

import static roomescape.fixture.ThemeFixture.DEFAULT_RESPONSE;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.ThemeResponse;

class ThemeResponseMapperTest {

    @Test
    @DisplayName("도메인을 응답으로 잘 변환하는지 확인")
    void toResponse() {
        ThemeResponse response = ThemeResponseMapper.toResponse(DEFAULT_THEME);

        Assertions.assertThat(response)
                .isEqualTo(DEFAULT_RESPONSE);
    }
}
