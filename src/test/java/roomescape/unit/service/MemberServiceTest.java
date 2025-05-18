package roomescape.unit.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.request.SignupRequest;
import roomescape.entity.Member;
import roomescape.exception.custom.InvalidMemberException;
import roomescape.global.Role;
import roomescape.repository.MemberRepository;
import roomescape.service.MemberService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void 멤버를_추가한다() {
        // given
        SignupRequest request = new SignupRequest("피케이", "pkkk@test.com", "test");
        Member member = request.toMember();

        when(memberRepository.save(any(Member.class))).thenReturn(new Member(
                1L,
                member.getName(),member.getEmail(),member.getPassword(), Role.USER)
        );
        when(memberRepository.existsByEmail(member.getEmail())).thenReturn(false);
        // when
        Member result = memberService.addMember(request);

        //then
        assertThat(result.getId()).isNotNull();
    }

    @Test
    void 동일한_이메일을_추가할_수_없다() {
        // given
        String sameEmail = "pkkk@test.com";
        when(memberRepository.existsByEmail(sameEmail)).thenReturn(true);

        SignupRequest request = new SignupRequest("피케케", sameEmail, "test");

        // when & then
        assertThatThrownBy(() -> memberService.addMember(request))
                .isInstanceOf(InvalidMemberException.class);
    }

    @Test
    void 모든_멤버를_찾는다() {
        // given
        when(memberRepository.findAll()).thenReturn(List.of(
                new Member("훌라", "hula@test.com", "test", Role.USER),
                new Member("피케이", "pkkk@test.com", "test", Role.USER)
        ));
        // when
        List<Member> result = memberService.findAll();

        //then
        assertThat(result).hasSize(2);
    }

    @Test
    void 이메일과_패스워드를_대조해_멤버를_찾는다() {
        // given
        LoginRequest request = new LoginRequest("hula@test.com", "test");
        memberRepository.save(new Member("훌라", request.email(), request.password(), Role.USER));
        when(memberRepository.findByEmailAndPassword(request.email(), request.password())).thenReturn(
                Optional.of(new Member("훌라", request.email(), request.password(), Role.USER)));

        // when
        Member member = memberService.findByEmailAndPassword(request);

        //then
        assertThat(member.getEmail()).isEqualTo(request.email());
    }

    @Test
    void 멤버_아이디를_통해_찾는다() {
        // given
        Member exMember = new Member(1L, "훌라", "hula@test.com", "test", Role.USER);
        when(memberRepository.save(exMember)).thenReturn(exMember);

        Member member = memberRepository.save(exMember);
        long memberId = member.getId();
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member result = memberService.getMemberById(memberId);

        //then
        assertThat(result.getId()).isEqualTo(memberId);
    }
}
