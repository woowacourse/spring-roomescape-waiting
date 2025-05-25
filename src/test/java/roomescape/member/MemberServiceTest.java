package roomescape.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.util.TestFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private MemberService memberService;

    @DisplayName("member를 생성하여 저장한다.")
    @Test
    void create() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");
        final Member expected = new Member(
                memberRequest.email(),
                memberRequest.password(),
                memberRequest.name(),
                MemberRole.MEMBER
        );

        // when
        memberService.create(memberRequest);

        // then
        then(memberRepository)
                .should()
                .save(expected);
    }

    @DisplayName("이미 존재하는 이메일로 생성하면, 예외가 발생한다.")
    @Test
    void create1() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");
        given(memberRepository.existsByEmail("admin@email.com"))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> {
            memberService.create(memberRequest);
        }).isInstanceOf(MemberEmailConflictException.class);
    }

    @DisplayName("존재하는 모든 member를 반환한다.")
    @Test
    void readAll() {
        // given
        given(memberRepository.findAll())
                .willReturn(List.of(TestFactory.memberWithId(1L, new Member("email", "pass", "name", MemberRole.MEMBER))));

        // when
        final List<MemberResponse> actual = memberService.getAll();

        // then
        assertThat(actual).hasSize(1);
    }

    @DisplayName("member가 없다면 빈 컬렉션을 반환한다.")
    @Test
    void readAll1() {
        // given & when
        final List<MemberResponse> actual = memberService.getAll();

        // then
        assertThat(actual).isEmpty();
    }
}
