package roomescape.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.then;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.PasswordEncoder;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.member.repository.MemberRepositoryImpl;

@DataJpaTest
@Sql(scripts = "classpath:/initialize_database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({
        MemberService.class,
        MemberRepositoryImpl.class,
        PasswordEncoder.class
})
class MemberServiceTest {

    @MockitoSpyBean
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberServiceTest(
            final MemberRepository memberRepository,
            final PasswordEncoder passwordEncoder,
            final MemberService memberService
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberService = memberService;
    }

    @DisplayName("member를 생성하여 저장한다.")
    @Test
    void createMember() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");
        final Member expected = new Member(memberRequest.email(), memberRequest.password(), memberRequest.name(),
                MemberRole.MEMBER);

        // when
        memberService.createMember(memberRequest);

        // then
        final ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        then(memberRepository).should().save(captor.capture());

        assertSoftly(s -> {
            final Member actual = captor.getValue();
            s.assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
            s.assertThat(passwordEncoder.matches(expected.getPassword(), actual.getPassword())).isTrue();
            s.assertThat(actual.getName()).isEqualTo(expected.getName());
            s.assertThat(actual.getRole()).isEqualTo(expected.getRole());
        });
    }

    @DisplayName("이미 존재하는 이메일로 생성하면, 예외가 발생한다.")
    @Test
    void createMember1() {
        // given
        final MemberRequest memberRequest = new MemberRequest("admin@email.com", "password", "부기");
        final Member member = new Member("admin@email.com", "password", "부기", MemberRole.MEMBER);
        memberRepository.save(member);

        // when & then
        assertThatThrownBy(() -> {
            memberService.createMember(memberRequest);
        }).isInstanceOf(MemberEmailConflictException.class);
    }

    @DisplayName("존재하는 모든 member를 반환한다.")
    @Test
    void readAll() {
        // given
        final Member member = new Member("email", "pass", "name", MemberRole.MEMBER);
        memberRepository.save(member);

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
