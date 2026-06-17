package roomescape.common.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;

class NameTest {

    @Nested
    class Constructor {

        @Test
        @DisplayName("2~15글자이면 생성에 성공한다")
        void createsWithValidName() {
            Name name = new Name("홍길동");

            assertThat(name.getValue()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("빈 문자열이면 예외를 던진다")
        void throwsWhenBlank() {
            assertThatThrownBy(() -> new Name("   "))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("1글자이면 예외를 던진다")
        void throwsWhenTooShort() {
            assertThatThrownBy(() -> new Name("홍"))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("16글자이면 예외를 던진다")
        void throwsWhenTooLong() {
            assertThatThrownBy(() -> new Name("가".repeat(16)))
                    .isInstanceOf(InvalidInputException.class);
        }
    }
}
