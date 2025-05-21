package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.request.MemberSignUpRequest;
import roomescape.auth.dto.response.MemberSignUpResponse;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.response.MemberNameSelectResponse;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 멤버를__추가할_수_있다() {

        // given
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest(
            member.getName(),
            member.getEmail(),
            member.getPassword()
        );

        // when
        MemberSignUpResponse actual = memberService.signup(signUpRequest);
        MemberSignUpResponse expected = new MemberSignUpResponse(member.getEmail(), true);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void id를_통해_존재하는_멤버를_찾는다() {

        // given
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        memberRepository.save(member);

        // when
        Member actual = memberService.findExistingMemberById(member.getId());

        // then
        assertThat(actual).isEqualTo(member);
    }

    @Test
    void principal을_통해_존재하는_멤버를_찾는다() {

        // given
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        memberRepository.save(member);

        // when
        Member actual = memberService.findExistingMemberByPrincipal(new MemberPrincipal(member.getName()));

        // then
        assertThat(actual).isEqualTo(member);
    }

    @Test
    void email을_통해_존재하는_멤버를_찾는다() {

        // given
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        memberRepository.save(member);

        // when
        Member actual = memberService.findByEmail(member.getEmail()).orElse(null);

        // then
        assertThat(actual).isEqualTo(member);
    }

    @Test
    void id를_통해_존재하는_멤버가_존재하는지_확인한다() {

        // given
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        memberRepository.save(member);

        // when
        boolean actual = memberService.isExistMemberById(member.getId());

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void 모든_멤버의_이름을_조회한다() {

        // given
        Member member1 = MemberFixture.createWithoutId(MemberRole.USER);
        Member member2 = MemberFixture.createWithoutId(MemberRole.USER);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<MemberNameSelectResponse> expected = List.of(
            new MemberNameSelectResponse(member1.getId(), member1.getName()),
            new MemberNameSelectResponse(member2.getId(), member2.getName())
        );

        // when
        List<MemberNameSelectResponse> actual = memberService.findMemberNames();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 이름을_통해_멤버가_존재하는지_확인한다() {

        // given
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        memberRepository.save(member);

        // when
        boolean actual = memberService.existsByName(member.getName());

        // then
        assertThat(actual).isTrue();
    }
}
