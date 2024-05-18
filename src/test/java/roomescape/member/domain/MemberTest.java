package roomescape.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.ViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.MIA_EMAIL;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.member.domain.Role.USER;

class MemberTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    @DisplayName("사용자 비밀번호는 비어있을 수 없다.")
    void validatePassword(String password) {
        // when & then
        assertThatThrownBy(() -> new Member(MIA_NAME, MIA_EMAIL, password, USER))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("사용자의 비밀번호를 확인한다.")
    void hasSamePassword() {
        // given
        Member member = new Member(MIA_NAME, MIA_EMAIL, "password", USER);

        // when
        boolean result = member.hasSamePassword("password");

        // then
        assertThat(result).isTrue();
    }
}
