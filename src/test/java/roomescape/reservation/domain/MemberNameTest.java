package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.MemberName;

class MemberNameTest {

    @Test
    @DisplayName("회원명에 한글이 아닌 값을 입력시 예외가 발생한다.")
    void createMemberNameByInvalidFormat() {
        assertThatThrownBy(() -> new MemberName("카ki"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("회원명이 4글자를 초과하면 예외가 발생한다.")
    void createMemberNameByLengthTest() {
        assertThatThrownBy(() -> new MemberName("abcde"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
