package roomescape.unit.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.MemberRole;

class MemberTest {

    @Test
    void name은_null일_수_없다() {
        // when // then
        assertThatThrownBy(
                () -> new Member(1L, null, new MemberEmail("a@a.com"), new MemberEncodedPassword("pw"), MemberRole.MEMBER))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void email은_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new Member(1L, new MemberName("홍길동"), null, new MemberEncodedPassword("pw"), MemberRole.MEMBER))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void password는_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new Member(1L, new MemberName("홍길동"), new MemberEmail("a@a.com"), null, MemberRole.MEMBER))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void role은_null일_수_없다() {
        // when // then
        assertThatThrownBy(
                () -> new Member(1L, new MemberName("홍길동"), new MemberEmail("a@a.com"), new MemberEncodedPassword("pw"), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 비밀번호가_일치하면_true를_반환한다() {
        // given
        var member = new Member(1L, new MemberName("홍길동"), new MemberEmail("a@a.com"),
                new MemberEncodedPassword("encoded"), MemberRole.ADMIN);
        var encoder = mock(PasswordEncoder.class);
        var rawPassword = new MemberPassword("abcde12!");

        // when
        when(encoder.matches(rawPassword.password(), "encoded")).thenReturn(true);

        // then
        assertThat(member.isMatchPassword(rawPassword, encoder)).isTrue();
    }
} 
