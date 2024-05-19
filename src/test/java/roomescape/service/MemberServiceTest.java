package roomescape.service;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.dto.request.SignupRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.support.fixture.MemberFixture;

class MemberServiceTest extends BaseServiceTest {

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
        Member member = MemberFixture.ADMIN;
        Member save = memberRepository.save(member);

        List<MemberResponse> memberResponses = memberService.getAllMembers();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberResponses).hasSize(1);
            softly.assertThat(memberResponses.get(0).id()).isEqualTo(save.getId());
        });
    }

    @Test
    @DisplayName("id로 회원을 조회한다.")
    void getById() {
        Member savedMember = memberRepository.save(MemberFixture.ADMIN);

        MemberResponse memberResponse = memberService.getById(savedMember.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberResponse.id()).isEqualTo(savedMember.getId());
        });
    }
}
