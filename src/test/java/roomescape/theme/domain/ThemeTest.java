package roomescape.theme.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ThemeTest {

    @Test
    void 생성_성공() {
        // given
        String name = "테마명";
        String description = "테마 설명";
        String thumbnail = "thumbnail.jpg";

        // when
        Theme theme = Theme.of(name, description, thumbnail);

        // then
        Assertions.assertThat(theme.getName()).isEqualTo(name);
        Assertions.assertThat(theme.getDescription()).isEqualTo(description);
        Assertions.assertThat(theme.getThumbnail()).isEqualTo(thumbnail);
    }

    @Test
    void null_값_입력_시_NullPointerException_발생() {
        // when & then
        Assertions.assertThatThrownBy(() -> Theme.of(null, "b", "c"))
                .isInstanceOf(NullPointerException.class);
        Assertions.assertThatThrownBy(() -> Theme.of("a", null, "c"))
                .isInstanceOf(NullPointerException.class);
        Assertions.assertThatThrownBy(() -> Theme.of("a", "b", null))
                .isInstanceOf(NullPointerException.class);
    }
}
