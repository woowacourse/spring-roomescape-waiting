package roomescape.domain.theme;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidInputException;

class ThumbnailTest {

    @Test
    @DisplayName("이름에 공백이 포함되면 예외가 발생한다.")
    void urlCannotContainSpace() {
        assertThatThrownBy(() -> new Thumbnail("공백 썸네일"))
            .isInstanceOf(InvalidInputException.class);
    }
}
