package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PasswordEncoderTest {

    PasswordEncoder passwordEncoder = new PasswordEncoder();

    @Test
    void 인코딩되지_않은_비밀번호와_인코딩된_비밀번호가_동일할_경우_true() {
        //given
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        //when
        boolean result = passwordEncoder.matches(rawPassword, encodedPassword);

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 인코딩되지_않은_비밀번호와_인코딩된_비밀번호가_동일하지_않을_경우_false() {
        //given
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode("654321");

        //when
        boolean result = passwordEncoder.matches(rawPassword, encodedPassword);

        //then
        assertThat(result).isFalse();
    }

    @Test
    void 비어있는_비밀번호를_인코딩할_경우_예외_발생() {
        //given
        String rawPassword = "";

        //when, then
        assertThatThrownBy(() -> passwordEncoder.encode(rawPassword))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
