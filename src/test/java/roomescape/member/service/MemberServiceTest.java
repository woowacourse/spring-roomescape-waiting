package roomescape.member.service;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 이메일에_해당하는_멤버_조회() {
        //given
        final String email = "east@email.com";
        final Member member = new Member(new MemberName("name"), new Email(email), new Password("password"), Role.USER);
        Member savedMember = memberRepository.save(member);

        //when & then
        assertThat(memberService.findMemberByEmail(email).getId()).isEqualTo(savedMember.getId());
    }

    @Test
    void 이메일에_해당하는_멤버가_없으면_예외_발생() {
        //given
        final String email = "no@email.com";

        //when & then
        Assertions.assertThatThrownBy(
                () -> memberService.findMemberByEmail(email)
        ).isInstanceOf(DataNotFoundException.class);
    }

    @Test
    void 모든_멤버_조회() {
        //when
        final List<Member> members = memberService.findAll();

        //then
        assertThat(members).isEmpty();
    }


    @TestConfiguration
    static class TestConfig {

        @Bean
        public MemberService memberService(
                final MemberRepository memberRepository
        ) {
            return new MemberService(memberRepository);
        }
    }
}
