package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.CustomInvalidDomainException;
import roomescape.exception.ErrorCode;

public class ThemeTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void nameBlankExceptionTest(String name) {
        assertThatThrownBy(() -> new Theme(name, "모험 이야기", "url.jpg"))
                .isInstanceOf(CustomInvalidDomainException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_NAME_NULL.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void descriptionBlankExceptionTest(String description) {
        assertThatThrownBy(() -> new Theme("피즈의 모험", description, "url.jpg"))
                .isInstanceOf(CustomInvalidDomainException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_DESCRIPTION_NULL.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void thumbnailUrlBlankExceptionTest(String thumbnailUrl) {
        assertThatThrownBy(() -> new Theme("피즈의 모험", "모험 이야기", thumbnailUrl))
                .isInstanceOf(CustomInvalidDomainException.class)
                .hasMessage(ErrorCode.NOT_ALLOW_THUMBNAIL_NULL.getMessage());
    }
}
