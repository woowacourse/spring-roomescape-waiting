package roomescape.unit.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.theme.ThemeThumbnail;

class ThemeThumbnailTest {

    @Test
    void null이면_예외를_던진다() {
        // when // then
        assertThatThrownBy(() -> new ThemeThumbnail(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 오십일자_이상이면_예외를_던진다() {
        // given
        var longUrl = "https://example.com/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.jpg";

        // when // then
        assertThatThrownBy(() -> new ThemeThumbnail(longUrl))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 정상_입력이면_생성된다() {
        // given
        var thumbnail = new ThemeThumbnail("thumb.jpg");

        // when // then
        assertThat(thumbnail.thumbnail()).isEqualTo("thumb.jpg");
    }
}
