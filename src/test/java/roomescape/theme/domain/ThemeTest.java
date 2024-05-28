package roomescape.theme.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.Fixtures;
import roomescape.exception.BadRequestException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("테마")
class ThemeTest {

    @DisplayName("테마 생성 시 이름, 설명, 썸네일 중 하나라도 비어있을 경우 예외가 발생한다.")
    @ValueSource(strings = {"", " ", "    ", "\n", "\r", "\t"})
    @ParameterizedTest
    void validateNotBlank(String blank) {
        // when & then
        String expectedMessage = "테마의 정보는 비어있을 수 없습니다.";

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThatThrownBy(() -> new Theme(blank, "description", "thumbnail"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
        softAssertions.assertThatThrownBy(() -> new Theme("name", blank, "thumbnail"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
        softAssertions.assertThatThrownBy(() -> new Theme("name", "description", blank))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
        softAssertions.assertAll();
    }

    @DisplayName("테마는 중복된 이름이 들어올 경우 예외가 발생한다.")
    @Test
    void validateDuplicatedName() {
        // given
        Theme theme = Fixtures.themeFixture;
        Theme other = new Theme(1L, theme.getName(), "겹치는 테마", theme.getThumbnail());

        // when & then
        assertThatThrownBy(() -> theme.validateDuplicatedName(other))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 테마 이름입니다.");
    }
}
