package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeTest {

    @Test
    @DisplayName("이름이 10자 초과이면 예외가 발생한다")
    void nameLengthException() {
        assertThatThrownBy(() -> new Theme(
            1L,
            "가".repeat(11),
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("설명이 50자 초과이면 예외가 발생한다")
    void descriptionLengthException() {
        assertThatThrownBy(() -> new Theme(
            1L,
            "레벨2 탈출",
            "가".repeat(51),
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
