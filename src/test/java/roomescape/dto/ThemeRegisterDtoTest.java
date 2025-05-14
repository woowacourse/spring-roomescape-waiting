package roomescape.dto;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.request.ThemeRegisterDto;

class ThemeRegisterDtoTest {

    @DisplayName("테마명은 null 일 수 없다.")
    @Test
    void test1() {
        assertThatThrownBy(() -> new ThemeRegisterDto(null, "s","s"))
                        .isInstanceOf(IllegalArgumentException.class);

    }

    @DisplayName("테마 설명은 null 일 수 없다.")
    @Test
    void test2() {
        assertThatThrownBy(() -> new ThemeRegisterDto("ㄴ", null,"s"))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @DisplayName("썸네일 이미지는 null 일 수 없다.")
    @Test
    void test3() {
        assertThatThrownBy(() -> new ThemeRegisterDto("ㅇ", "s",null))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @DisplayName("테마명은 공백 일 수 없다.")
    @Test
    void test4() {
        assertThatThrownBy(() -> new ThemeRegisterDto("", "s","s"))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @DisplayName("테마 설명은 공백 일 수 없다.")
    @Test
    void test5() {
        assertThatThrownBy(() -> new ThemeRegisterDto("ㅇ", "","s"))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @DisplayName("썸내일 이미지는 공백 일 수 없다.")
    @Test
    void test6() {
        assertThatThrownBy(() -> new ThemeRegisterDto("ㅇ", "s",""))
                .isInstanceOf(IllegalArgumentException.class);

    }
}
