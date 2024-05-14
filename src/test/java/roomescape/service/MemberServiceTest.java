package roomescape.service;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.config.TestConfig;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.dto.request.SignupRequest;
import roomescape.dto.response.MemberResponse;

@SpringBootTest(classes = TestConfig.class)
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원을 생성한다.")
    void createMember() {
        SignupRequest request = new SignupRequest("new@gmail.com", "password", "nickname");

        MemberResponse memberResponse = memberService.createMember(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberResponse.email()).isEqualTo("new@gmail.com");
            softly.assertThat(memberResponse.name()).isEqualTo("nickname");
            softly.assertThat(memberResponse.role()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("모든 회원을 조회한다.")
    void getAllMembers() {
        Member member = new Member("new@gmail.com", "password", "nickname", Role.USER);
        Member save = memberRepository.save(member);

        List<MemberResponse> memberResponses = memberService.getAllMembers();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberResponses).hasSize(1);
            softly.assertThat(memberResponses.get(0).id()).isEqualTo(save.getId());
            softly.assertThat(memberResponses.get(0).email()).isEqualTo("new@gmail.com");
            softly.assertThat(memberResponses.get(0).name()).isEqualTo("nickname");
            softly.assertThat(memberResponses.get(0).role()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("id로 회원을 조회한다.")
    void getById() {
        Member member = new Member("new@gmail.com", "password", "nickname", Role.USER);
        Member savedMember = memberRepository.save(member);

        MemberResponse memberResponse = memberService.getById(savedMember.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberResponse.email()).isEqualTo("new@gmail.com");
            softly.assertThat(memberResponse.name()).isEqualTo("nickname");
            softly.assertThat(memberResponse.role()).isEqualTo(Role.USER);
        });
    }
}
