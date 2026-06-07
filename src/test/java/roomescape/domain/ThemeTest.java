package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.custom.InvalidDomainValueException;

public class ThemeTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void nameBlankExceptionTest(String name) {
        assertThatThrownBy(() -> new Theme(name, "모험 이야기", "url.jpg"))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void descriptionBlankExceptionTest(String description) {
        assertThatThrownBy(() -> new Theme("피즈의 모험", description, "url.jpg"))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void thumbnailUrlBlankExceptionTest(String thumbnailUrl) {
        assertThatThrownBy(() -> new Theme("피즈의 모험", "모험 이야기", thumbnailUrl))
                .isInstanceOf(InvalidDomainValueException.class);
    }
}
