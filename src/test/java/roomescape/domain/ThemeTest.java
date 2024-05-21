package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

class ThemeTest {

    @DisplayName("테마의 설명이 255자를 초과하면 예외가 발생한다.")
    @Test
    void createExceptionTest() {
        String description = "-".repeat(256);
        assertThatCode(() -> new Theme(new ThemeName("test"), description, "thumbnail"))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("테마의 썸네일이 없으면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest
    void createExceptionTest2(String thumbnail) {
        assertThatCode(() -> new Theme(new ThemeName("test"), "description", thumbnail))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }
}
