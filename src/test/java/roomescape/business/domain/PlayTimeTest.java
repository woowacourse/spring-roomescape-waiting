package roomescape.business.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlayTimeTest {

    @Test
    @DisplayName("startAt 필드에 null 들어오면 예외가 발생한다")
    void validateStartAt() {
        // given
        final LocalTime invalidStartAt = null;

        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> new PlayTime(invalidStartAt))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> new PlayTime(1L, invalidStartAt))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }
}
