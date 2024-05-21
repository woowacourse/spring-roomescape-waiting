package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeTest {

    @DisplayName("이름, 설명, 섬네일 중 하나라도 빈 값이 있을 경우 예외가 발생한다.")
    @CsvSource({",description,thumbnail", "name,,thumbnail", "name,description,"})
    @ParameterizedTest
    void given_when_newWithEmptyValue_then_throwException(String name, String description, String thumbnail) {
        //given, when, then
        assertThatThrownBy(() -> new Theme(1L, name, description, thumbnail))
                .isInstanceOf(EmptyValueNotAllowedException.class);
    }

}