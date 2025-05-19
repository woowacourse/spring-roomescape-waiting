package roomescape.unit.domain.theme;

import org.junit.jupiter.api.Test;
import roomescape.domain.theme.ThemeThumbnail;

import static org.assertj.core.api.Assertions.*;

class ThemeThumbnailTest {

    @Test
    void null이면_예외를_던진다() {
        assertThatThrownBy(() -> new ThemeThumbnail(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 오십일자_이상이면_예외를_던진다() {
        String longUrl = "https://example.com/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.jpg";
        assertThatThrownBy(() -> new ThemeThumbnail(longUrl))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 정상_입력이면_생성된다() {
        ThemeThumbnail thumbnail = new ThemeThumbnail("thumb.jpg");
        assertThat(thumbnail.thumbnail()).isEqualTo("thumb.jpg");
    }
}
