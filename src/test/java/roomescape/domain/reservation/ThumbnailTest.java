package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class ThumbnailTest {

    @Test
    @DisplayName("문자열을 통해 도메인을 생성한다.")
    void create_domain_with_string() {
        assertThatCode(() -> new Thumbnail("https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"))
                .doesNotThrowAnyException();
    }
}
