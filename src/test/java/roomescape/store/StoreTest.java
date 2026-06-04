package roomescape.store;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class StoreTest {

    @Test
    void 정상_생성된다() {
        assertThatCode(() -> new Store(1L, "강남점"))
                .doesNotThrowAnyException();
    }

    @Test
    void 이름이_null이면_매장을_생성할_수_없다() {
        assertThatThrownBy(() -> new Store(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매장 이름은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_공백이면_매장을_생성할_수_없다() {
        assertThatThrownBy(() -> new Store(1L, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매장 이름은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_50자_초과면_매장을_생성할_수_없다() {
        String tooLong = "a".repeat(51);
        assertThatThrownBy(() -> new Store(1L, tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매장 이름은 50자 이하여야 합니다.");
    }
}
