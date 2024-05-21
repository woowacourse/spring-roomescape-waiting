package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NameTest {

    @Test
    void 이름에_한글_또는_영어가_아닌_문자가_포함된_경우_예외_발생() {
        //given
        String name = "이름1";

        //when, then
        assertThatThrownBy(() -> new Name(name))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이름이_2자_미만일_경우_예외_발생() {
        //given
        String name = "일";

        //when, then
        assertThatThrownBy(() -> new Name(name))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이름이_10자_초과일_경우_예외_발생() {
        //given
        String name = "일이삼사오육칠팔구십일";

        //when, then
        assertThatThrownBy(() -> new Name(name))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
