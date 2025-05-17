package roomescape.application;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.domain.Member;
import roomescape.fixture.MemberDbFixture;
import roomescape.presentation.dto.request.MemberCreateRequest;
import roomescape.presentation.dto.response.MemberResponse;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MemberServiceTest extends BaseTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberDbFixture memberDbFixture;

    @Test
    void 이메일로_회원을_조회한다() {
        Member member = memberDbFixture.한스_사용자();
        Member findMember = memberService.findMemberByEmail(member.getEmail());

        assertThat(member).isEqualTo(findMember);
    }

    @Test
    void 존재하지_않는_이메일로_조회하면_예외가_발생한다() {
        String notExistEmail = "email@email.com";
        assertThatThrownBy(() -> memberService.findMemberByEmail(notExistEmail))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 멤버를_모두_조회한다() {
        Member member = memberDbFixture.한스_사용자();
        List<MemberResponse> responses = memberService.getMembers();
        MemberResponse response = responses.getFirst();

        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(response.id()).isEqualTo(member.getId()),
                () -> assertThat(response.name()).isEqualTo(member.getName()),
                () -> assertThat(response.email()).isEqualTo(member.getEmail())
        );
    }

    @Test
    void 멤버를_생성한다() {
        MemberCreateRequest request = new MemberCreateRequest("듀이", "test@email.com", "pass1");

        MemberResponse response = memberService.createMember(request);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.name()).isEqualTo(request.name()),
                () -> assertThat(response.email()).isEqualTo(request.email())
        );
    }

    @Test
    void 아이디로_회원을_조회한다() {
        Member member = memberDbFixture.한스_사용자();
        Member findMember = memberService.findMemberById(member.getId());

        assertThat(member).isEqualTo(findMember);
    }

    @Test
    void 존재하지_않는_아이디로_조회하면_예외가_발생한다() {
        Long notExistId = 2L;
        assertThatThrownBy(() -> memberService.findMemberById(notExistId))
                .isInstanceOf(NoSuchElementException.class);
    }
}
