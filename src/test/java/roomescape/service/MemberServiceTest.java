package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.member.Member;
import roomescape.dto.MemberResponse;
import roomescape.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static roomescape.TestFixture.ADMIN;
import static roomescape.TestFixture.MEMBER_CAT;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("id에 해당하는 사용자를 조회한다.")
    void findById() {
        // given
        final Member expectedMember = MEMBER_CAT(1L);
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.of(expectedMember));

        // when
        final MemberResponse memberResponse = memberService.findById(1L);

        // then
        assertThat(memberResponse.id()).isEqualTo(expectedMember.getId());
    }

    @Test
    @DisplayName("모든 사용자를 조회한다.")
    void findAll() {
        // given
        final Member member1 = MEMBER_CAT(1L);
        final Member member2 = ADMIN(2L);
        final List<Member> initial_members = List.of(member1, member2);
        given(memberRepository.findAll()).willReturn(initial_members);

        // when
        final List<MemberResponse> members = memberService.findAll();

        // then
        final MemberResponse memberResponse1 = MemberResponse.from(member1);
        final MemberResponse memberResponse2 = MemberResponse.from(member2);
        assertThat(members).hasSize(2)
                .containsExactly(memberResponse1, memberResponse2);
    }
}
