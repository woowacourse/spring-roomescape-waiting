package roomescape.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ThemeDto;

import static org.assertj.core.api.Assertions.*;

class ThemeTest {

    @DisplayName("데이터의 id가 0 이하인 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})
    void should_throw_exception_when_invalid_id(long id) {
        assertThatThrownBy(() -> new Theme(id, "n", "d", "t"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("id는 0 이하일 수 없습니다.");
    }

    @DisplayName("데이터의 이름이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_name(String name) {
        assertThatThrownBy(() -> new Theme(1L, name, "d", "t"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("테마 이름은 null 혹은 빈 문자열일 수 없습니다.");
    }

    @DisplayName("데이터의 설명이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_description(String description) {
        assertThatThrownBy(() -> new Theme(1L, "n", description, "t"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("테마 설명문은 null 혹은 빈 문자열일 수 없습니다.");
    }

    @DisplayName("데이터의 썸네일이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_thumbnail(String thumbnail) {
        assertThatThrownBy(() -> new Theme(1L, "n", "d", thumbnail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("테마 썸네일은 null 혹은 빈 문자열일 수 없습니다.");
    }

    @DisplayName("데이터가 유효한 경우 예외가 발생하지 않는다.")
    @ParameterizedTest
    @CsvSource(value = {"1,nnn,ddd,ttt", "1,111,222,333", "1, n , d   , t"})
    void should_not_throw_exception_when_valid_data(long id, String name, String description, String thumbnail) {
        assertThatCode(() -> new Theme(id, name, description, thumbnail))
                .doesNotThrowAnyException();
    }

    @DisplayName("DTO를 도메인으로 변환하는 경우 id는 0이 될 수 있다.")
    @Test
    void should_be_zero_id_when_convert_dto() {
        ThemeDto themeDto = new ThemeDto("n", "d", "t");
        assertThatCode(() -> {
            Theme theme = Theme.from(themeDto);
            assertThat(theme.getId()).isEqualTo(0);
        }).doesNotThrowAnyException();
    }
}
