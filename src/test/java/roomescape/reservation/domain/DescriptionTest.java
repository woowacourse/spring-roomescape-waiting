package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DescriptionTest {

    @Test
    @DisplayName("설명에 공백, 쉼표, 구두점을 포함할 수 있고 한글 또는 숫자가 아닌 값을 입력시 예외가 발생한다.")
    void createDescriptionByInvalidFormat() {
        assertThatThrownBy(() -> new Description("경험했던, scare 이야기 입니다."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("설명이 50글자를 초과하면 예외가 발생한다.")
    void createDescriptionByLengthTest() {
        String input = "가나다라마바사아자차".repeat(5) + "1";

        assertThatThrownBy(() -> new Description(input))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
