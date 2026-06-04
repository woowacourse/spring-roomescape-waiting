package roomescape.domain.theme;

import common.exception.RoomEscapeException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ThumbnailUrlTest {
    @Test
    void 유효하지_않은_형식으로_생성시_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> new ThumbnailUrl("zeze.com")).isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void NULL이_입력된_경우_예외가_발생한다() {
        Assertions.assertThatThrownBy(() -> new ThumbnailUrl(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void 유효한_요청인_경우_예외가_발생하지_않는다() {
        Assertions.assertThatNoException().isThrownBy(() -> new ThumbnailUrl("https://zeze.com"));
    }
}
