package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.auth.service.MyPasswordEncoder;
import roomescape.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.dto.request.SignupRequest;
import roomescape.member.dto.response.MemberResponse;
import roomescape.member.dto.response.SignUpResponse;
import roomescape.member.repository.MemberRepository;

@DataJpaTest
@Import(TestConfig.class)
public class MemberDomainServiceTest {

    private MemberApplicationService memberApplicationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MyPasswordEncoder myPasswordEncoder;

    @BeforeEach
    void setUp() {
        memberApplicationService = new MemberApplicationService(new MemberDomainService(memberRepository),
                myPasswordEncoder);
    }

    @Test
    void signUpTest() {
        SignUpResponse response = memberApplicationService.signup(
                new SignupRequest("member@gmail.com", "password", "member"));

        Optional<Member> optionalMember = memberRepository.findById(response.id());
        assertThat(optionalMember.get().getName()).isEqualTo("member");
    }

    @Test
    void findAllRegularTest() {
        memberApplicationService.signup(new SignupRequest("member1@gmail.com", "password", "member1"));
        memberApplicationService.signup(new SignupRequest("member2@gmail.com", "password", "member2"));
        memberApplicationService.signup(new SignupRequest("member3@gmail.com", "password", "member3"));
        List<MemberResponse> memberResponses = memberApplicationService.findAllRegularMembers();
        assertThat(memberResponses.size()).isEqualTo(3);
    }
}
