package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThumbnailTest {

    @Test
    @DisplayName("문자열을 통해 도메인을 생성한다.")
    void create_domain_with_string() {
        assertThatCode(() -> new Thumbnail("https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("지정된 형식자로 끝나지 않으면 예외를 발생한다.")
    void throw_exception_when_create_with_not_allowed_extension() {
        assertThatThrownBy(() -> new Thumbnail("https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
