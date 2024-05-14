package roomescape.domain.Theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DescriptionTest {

    @Test
    void 설명이_10자_미만일_경우_예외_발생() {
        //given
        String name = "123456789";

        //when, then
        assertThatThrownBy(() -> new Description(name))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
