package roomescape.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MemberTest {

    @DisplayName("비밀번호가 일치하면 true, 아니라면 false를 반환한다.")
    @ParameterizedTest
    @CsvSource(value = {"password:true", "not matches password:false"}, delimiter = ':')
    void matchesPassword(final String password, final boolean expected) {
        // given
        final Member member = new Member("email", "password", "name", MemberRole.MEMBER);

        // when
        final boolean actual = member.matchesPassword(password);

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
