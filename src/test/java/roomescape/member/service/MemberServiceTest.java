package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void MemberRequest를_전달하면_repository_save가_호출된다() {
        // given
        MemberRequest request = new MemberRequest("hong@example.com", "password", "홍길동");

        // when
        memberService.save(request);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member saved = captor.getValue();

        assertThat(saved.getId()).isNull();
        assertThat(saved.getName()).isEqualTo("홍길동");
        assertThat(saved.getEmail()).isEqualTo("hong@example.com");
        assertThat(saved.getPassword()).isEqualTo("password");
        assertThat(saved.getRole()).isEqualTo(MemberRole.MEMBER);
    }

    @Test
    void repository_findAll_결과가_MemberResponse_리스트로_매핑된다() {
        // given
        Member m1 = new Member(1L, "A", "a@a.com", "pw", "ADMIN");
        Member m2 = new Member(2L, "B", "b@b.com", "pw", "MEMBER");
        when(memberRepository.findAll()).thenReturn(List.of(m1, m2));

        // when
        List<MemberResponse> responses = memberService.findAllMember();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("A");
        assertThat(responses.get(0).email()).isEqualTo("a@a.com");
        assertThat(responses.get(0).role()).isEqualTo(MemberRole.ADMIN.name());

        assertThat(responses.get(1).name()).isEqualTo("B");
        assertThat(responses.get(1).email()).isEqualTo("b@b.com");
        assertThat(responses.get(1).role()).isEqualTo(MemberRole.MEMBER.name());
    }
}
