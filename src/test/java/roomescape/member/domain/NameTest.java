package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.exception.InvalidNameException;
import roomescape.member.exception.NameRequiredException;

class NameTest {

    @DisplayName("이름은 빈 값일 수 없다")
    @Test
    void name_blank() {
        // given
        String value = "";

        // when & then
        assertThatThrownBy(() -> new Name(value))
                .isInstanceOf(NameRequiredException.class);
    }

    @DisplayName("이름은 특수문자를 허용하지 않는다")
    @Test
    void name_specialCharacter() {
        // given
        String value = "#에드";

        // when & then
        assertThatThrownBy(() -> new Name(value))
                .isInstanceOf(InvalidNameException.class);
    }
}
