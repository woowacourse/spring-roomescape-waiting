package roomescape.member.service;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.common.exception.DataNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.JdbcMemberRepository;
import roomescape.member.repository.MemberRepository;

@JdbcTest
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    void 이메일에_해당하는_멤버_조회() {
        //given
        final String email = "east@email.com";

        //when & then
        Assertions.assertThatCode(() -> memberService.findMemberByEmail(email))
                .doesNotThrowAnyException();
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
        Assertions.assertThat(members).isNotEmpty();
    }


    @TestConfiguration
    static class TestConfig {

        @Bean
        public MemberRepository memberRepository(
                final JdbcTemplate jdbcTemplate
        ) {
            return new JdbcMemberRepository(jdbcTemplate);
        }

        @Bean
        public MemberService memberService(
                final MemberRepository memberRepository
        ) {
            return new MemberService(memberRepository);
        }
    }
}
