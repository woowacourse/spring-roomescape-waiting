package roomescape.reservation.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerNameTest {

    @Test
    void 이름을_생성한다() {
        CustomerName customerName = new CustomerName("브라운");

        assertThat(customerName.name()).isEqualTo("브라운");
    }

    @Test
    void 이름이_비어있으면_예외가_발생한다() {
        assertThatThrownBy(() -> new CustomerName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름을 입력해야 합니다.");
    }

    @Test
    void 이름이_10자를_초과하면_예외가_발생한다() {
        assertThatThrownBy(() -> new CustomerName("가나다라마바사아자차카"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 10자 이하여야 합니다.");
    }
}
