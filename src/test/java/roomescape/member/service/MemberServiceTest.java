package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;

@DataJpaTest
@Import(MemberService.class)
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;

    @DisplayName("이메일과 패스워드를 알려주면 일치하는 사용자를 찾아온다.")
    @Test
    void findMemberByEmailAndPassword() {
        String email = "if@posty.com";
        String password = "12345678";
        memberRepository.save(new Member("이프", email, password, Role.MEMBER));

        Member findMember = memberService.findMemberByEmailAndPassword(email, password);

        assertAll(
                () -> assertThat(findMember.getEmail()).isEqualTo(email),
                () -> assertThat(findMember.getPassword()).isEqualTo(password)
        );
    }

    @DisplayName("이메일 또는 패스워드가 맞지 않으면 사용자를 찾아올 수 없다.")
    @CsvSource(value = {"posty@if.com:12345678", "if@posty.com:87654321"}, delimiter = ':')
    @ParameterizedTest
    void findMemberByUnmatchedEmailAndPassword(String unmatchedEmail, String unmatchedPassword) {
        String email = "if@posty.com";
        String password = "12345678";
        memberRepository.save(new Member("이프", email, password, Role.MEMBER));

        assertThatThrownBy(() -> memberService.findMemberByEmailAndPassword(unmatchedEmail, unmatchedPassword))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
