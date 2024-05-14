package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

@DisplayName("테마 도메인 테스트")
class ThemeTest {
    @DisplayName("동일한 id는 같은 테마다.")
    @Test
    void equals() {
        //given
        long id1 = 1;
        String name1 = "name1";
        String description1 = "description1";
        String thumbnail1 = "thumbnail1";

        String name2 = "name2";
        String description2 = "description2";
        String thumbnail2 = "thumbnail2";

        //when
        Theme theme1 = new Theme(id1, name1, description1, thumbnail1);
        Theme theme2 = new Theme(id1, name2, description2, thumbnail2);

        //then
        assertThat(theme1).isEqualTo(theme2);
    }

    @DisplayName("문자열 필드에 빈칸을 허용하지 않는다.")
    @ParameterizedTest
    @ValueSource(strings = {"", "    "})
    void invalidStings(String value) {
        //given
        long id = 1;
        String name = "name1";
        String description = "description1";
        String thumbnail = "thumbnail1";

        //when & then
        assertAll(
                () -> assertThatThrownBy(() -> new Theme(id, value, description, thumbnail))
                        .isInstanceOf(BusinessException.class)
                        .hasMessage(ErrorType.MISSING_REQUIRED_VALUE_ERROR.getMessage()),
                () -> assertThatThrownBy(() -> new Theme(id, name, value, thumbnail))
                        .isInstanceOf(BusinessException.class)
                        .hasMessage(ErrorType.MISSING_REQUIRED_VALUE_ERROR.getMessage()),
                () -> assertThatThrownBy(() -> new Theme(id, name, description, value))
                        .isInstanceOf(BusinessException.class)
                        .hasMessage(ErrorType.MISSING_REQUIRED_VALUE_ERROR.getMessage())
        );
    }
}
