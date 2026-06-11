package roomescape.domain.reservation;

import common.exception.RoomEscapeException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RankTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 999})
    void 정상적인_입력은_예외가_발생하지_않는다(int value) {
        Assertions.assertThatNoException().isThrownBy(() -> new Rank(value));
    }

    @ParameterizedTest
    @ValueSource(ints = {-999, -1})
    void 잘못된_입력은_예외가_발생한다(int value) {
        Assertions.assertThatException().isThrownBy(() -> new Rank(value)).isInstanceOf(RoomEscapeException.class);
    }
}
