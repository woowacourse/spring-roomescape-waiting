package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.request.SignupRequest;
import roomescape.application.dto.response.MemberResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.exception.BadRequestException;

class MemberServiceTest extends BaseServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원을 생성한다.")
    void createMember() {
        SignupRequest request = new SignupRequest("ex@gmail.com", "password", "nickname");

        MemberResponse memberResponse = memberService.createMember(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberResponse.email()).isEqualTo("ex@gmail.com");
            softly.assertThat(memberResponse.name()).isEqualTo("nickname");
            softly.assertThat(memberResponse.role()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("회원을 생성할 때, 이미 존재하는 이메일이면 예외를 발생시킨다.")
    void createMemberFailWhenEmailAlreadyExists() {
        memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));

        SignupRequest request = new SignupRequest("ex@gmail.com", "password", "nickname");

        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(String.format("해당 이메일의 회원이 이미 존재합니다. (email: %s)", request.email()));
    }

    @Test
    @DisplayName("모든 회원들을 조회한다.")
    void getAllMembers() {
        Member member = new Member("ex@gmail.com", "password", "nickname", Role.USER);
        Member save = memberRepository.save(member);

        List<MemberResponse> memberResponses = memberService.getAllMembers();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberResponses).hasSize(1);
            softly.assertThat(memberResponses.get(0).id()).isEqualTo(save.getId());
            softly.assertThat(memberResponses.get(0).email()).isEqualTo("ex@gmail.com");
            softly.assertThat(memberResponses.get(0).name()).isEqualTo("nickname");
            softly.assertThat(memberResponses.get(0).role()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("id로 회원을 조회한다.")
    void getById() {
        Member member = new Member("ex@gmail.com", "password", "nickname", Role.USER);
        Member savedMember = memberRepository.save(member);

        MemberResponse memberResponse = memberService.getById(savedMember.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(memberResponse.email()).isEqualTo("ex@gmail.com");
            softly.assertThat(memberResponse.name()).isEqualTo("nickname");
            softly.assertThat(memberResponse.role()).isEqualTo(Role.USER);
        });
    }
}
