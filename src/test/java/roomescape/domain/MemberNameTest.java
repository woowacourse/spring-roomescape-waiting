package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MemberNameTest {

    @DisplayName("예약자명은 최소 1자 최대 10자 이하이다.")
    @ValueSource(strings = {"a", "rush", "abcdefghij"})
    @ParameterizedTest
    void nameLength(String name) {
        assertThatCode(() -> new MemberName(name))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약자명이 1자 미만, 10자 초과일 경우 예외처리")
    @ValueSource(strings = {"", " ", "abcdefghijk"})
    @ParameterizedTest
    void nameLength2(String name) {
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @DisplayName("예약자명은 한글 또는 영어이다.")
    @ValueSource(strings = {"러쉬!", "rush1", "1수달", "1", "!@", "수1달"})
    @ParameterizedTest
    void koreanOrEnglishName(String name) {
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(IllegalArgumentException.class);
    }


}
