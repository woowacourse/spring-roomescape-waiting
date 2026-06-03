package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CustomerNameTest {

    @Test
    void 예약자_이름은_10자_내로_생성할_수_있다() {
        assertThatCode(() -> new CustomerName("n".repeat(10)))
            .doesNotThrowAnyException();
    }

    @Test
    void 예약자_이름이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new CustomerName(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void 예약자_이름이_blank이면_예외가_발생한다() {
        assertThatThrownBy(() -> new CustomerName(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약자_이름_길이가_10자를_초과하면_예외가_발생한다() {
        assertThatThrownBy(() -> new CustomerName("n".repeat(11)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
