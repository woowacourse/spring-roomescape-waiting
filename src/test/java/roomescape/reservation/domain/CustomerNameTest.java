package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerNameTest {

    @Test
    @DisplayName("이름을 생성한다")
    void createName() {
        CustomerName customerName = new CustomerName("브라운");

        assertThat(customerName.name()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("이름이 비어있으면 예외가 발생한다")
    void throwExceptionWhenNameIsBlank() {
        assertThatThrownBy(() -> new CustomerName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름을 입력해야 합니다.");
    }

    @Test
    @DisplayName("이름이 10자를 초과하면 예외가 발생한다")
    void throwExceptionWhenNameExceedsTenCharacters() {
        assertThatThrownBy(() -> new CustomerName("가나다라마바사아자차카"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 10자 이하여야 합니다.");
    }
}
