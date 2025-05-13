package roomescape.application;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.infrastructure.repository.MemberRepository;
import roomescape.presentation.dto.request.MemberCreateRequest;
import roomescape.presentation.dto.response.MemberResponse;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class MemberServiceWithMockTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void 이메일로_회원을_조회한다() {
        Member member = Member.create("듀이", Role.USER, "email@email.com", "pass1");
        given(memberRepository.findByEmail(member.getEmail()))
                .willReturn(Optional.of(member));

        Member findMember = memberService.findMemberByEmail(member.getEmail());

        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void 존재하지_않는_이메일로_조회하면_예외가_발생한다() {
        String notExistEmail = "email@email.com";
        when(memberRepository.findByEmail(notExistEmail))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findMemberByEmail(notExistEmail))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 멤버를_모두_조회한다() {
        Member member = Member.create("듀이", Role.USER, "email@email.com", "pass1");
        when(memberRepository.findAll())
                .thenReturn(List.of(member));

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

        Member member = Member.create(request.name(), Role.USER, request.email(), request.password());
        when(memberRepository.save(member))
                .thenReturn(Member.create(1L, request.name(), Role.USER, request.email(), request.password()));

        MemberResponse response = memberService.createMember(request);

        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.name()).isEqualTo(request.name()),
                () -> assertThat(response.email()).isEqualTo(request.email())
        );
    }

    @Test
    void 아이디로_회원을_조회한다() {
        Member member = Member.create("듀이", Role.USER, "email@email.com", "pass1");
        when(memberRepository.findById(member.getId()))
                .thenReturn(Optional.of(member));

        Member findMember = memberService.findMemberById(member.getId());

        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void 존재하지_않는_아이디로_조회하면_예외가_발생한다() {
        Long notExistId = 2L;

        assertThatThrownBy(() -> memberService.findMemberById(notExistId))
                .isInstanceOf(NoSuchElementException.class);
    }
}
