package roomescape.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;

public class MemberServiceTest {

    private final MemberService memberService;
    private final FakeMemberRepository fakeMemberRepository;

    public MemberServiceTest() {
        fakeMemberRepository = new FakeMemberRepository();
        memberService = new MemberService(fakeMemberRepository);
    }

    @BeforeEach
    void setUp() {
        fakeMemberRepository.clear();
    }

    @DisplayName("member를 생성하여 저장한다.")
    @Test
    void createMember() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");

        // when
        memberService.createMember(memberRequest);

        // then
        assertThat(fakeMemberRepository.isInvokeSaveMemberEmail("admin@email.com")).isTrue();
    }

    @DisplayName("이미 존재하는 이메일로 생성하면, 예외가 발생한다.")
    @Test
    void createMember1() {
        // given
        memberService.createMember(new MemberRequest("admin@email.com", "1", "2"));
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");

        // when & then
        assertThatThrownBy(() -> {
            memberService.createMember(memberRequest);
        }).isInstanceOf(MemberEmailConflictException.class);
    }

    @DisplayName("존재하는 모든 member를 반환한다.")
    @Test
    void readAll() {
        // given
        fakeMemberRepository.saveMember(new Member("email", "pass", "name", MemberRole.MEMBER));

        // when
        final List<MemberResponse> actual = memberService.readAllMember();

        // then
        assertThat(actual).hasSize(1);
    }

    @DisplayName("member가 없다면 빈 컬렉션을 반환한다.")
    @Test
    void readAll1() {
        // given & when
        final List<MemberResponse> actual = memberService.readAllMember();

        // then
        assertThat(actual).isEmpty();
    }
}
