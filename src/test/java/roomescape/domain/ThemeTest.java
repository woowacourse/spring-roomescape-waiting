package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.domain.exception.DomainErrorCode;

public class ThemeTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void nameBlankExceptionTest(String name) {
        assertThatThrownBy(() -> new Theme(name, "모험 이야기", "url.jpg"))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.INVALID_INPUT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void descriptionBlankExceptionTest(String description) {
        assertThatThrownBy(() -> new Theme("피즈의 모험", description, "url.jpg"))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.INVALID_INPUT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void thumbnailUrlBlankExceptionTest(String thumbnailUrl) {
        assertThatThrownBy(() -> new Theme("피즈의 모험", "모험 이야기", thumbnailUrl))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.INVALID_INPUT));
    }
}
