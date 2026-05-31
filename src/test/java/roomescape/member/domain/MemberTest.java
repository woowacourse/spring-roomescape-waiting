package roomescape.member.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.exception.MemberException;

import static roomescape.member.exception.MemberExceptionInformation.PASSWORD_NOT_MATCH;

class MemberTest {

    @Test
    @DisplayName("회원가입 시, id를 제외하고 Member 객체가 생성된다.")
    void register() {
        // given
        String name = "송송";
        String rawPassword = "1234";

        // when
        Member songsong = Member.register(name, rawPassword);

        // then
        Assertions.assertThat(songsong.getId()).isNull();
        Assertions.assertThat(songsong.getName()).isEqualTo(name);
        Assertions.assertThat(songsong.getRole()).isEqualTo(Role.MEMBER);
    }

    @Test
    @DisplayName("회원의 비밀번호가 틀리면 예외가 발생한다.")
    void matchPassword_wrong_password() {
        // given
        Member songsong = Member.register("송송", "1234");
        String wrongPassword = "wrongPassword";

        // when & then
        Assertions.assertThatThrownBy(() -> songsong.matchPassword(wrongPassword))
                .isInstanceOf(MemberException.class)
                .hasMessage(PASSWORD_NOT_MATCH.getMessage());
    }

}
