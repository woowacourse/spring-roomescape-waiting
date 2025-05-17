package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.global.auth.service.MyPasswordEncoder;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.SignupRequest;
import roomescape.member.repository.MemberRepository;

@DataJpaTest
@Import({MyPasswordEncoder.class})
@TestPropertySource(properties = {
        "spring.sql.init.data-locations="
})
public class MemberServiceTest {

    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MyPasswordEncoder myPasswordEncoder;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, myPasswordEncoder);
    }

    @Test
    void signUpTest() {
        Member member = memberService.signup(new SignupRequest("userr@gmail.com", "password", "userr"));

        Optional<Member> optionalMember = memberRepository.findById(member.getId());
        assertThat(optionalMember.get().getName()).isEqualTo("userr");
    }

    @Test
    void findAllUsersTest() {
        Member member = memberService.signup(new SignupRequest("userr@gmail.com", "password", "userr"));
        Member member2 = memberService.signup(new SignupRequest("userrr@gmail.com", "password", "userrr"));
        Member member3 = memberService.signup(new SignupRequest("userrrr@gmail.com", "password", "userrrr"));
        List<MemberResponse> memberResponses= memberService.findAllUsers();
        assertThat(memberResponses.size()).isEqualTo(3);
    }
}
