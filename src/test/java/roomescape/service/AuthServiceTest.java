package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.request.MemberRegisterRequest;
import roomescape.dto.response.MemberRegisterResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.MemberRepositoryImpl;
import roomescape.repository.jpa.MemberJpaRepository;

@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DataJpaTest
class AuthServiceTest {

    private AuthService authService;

    private MemberService memberService;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManager entityManager;


    @BeforeEach
    void setUp() {
        MemberRepository memberRepository = new MemberRepositoryImpl(memberJpaRepository, new JdbcTemplate(dataSource));
        authService = new AuthService(memberRepository);
        memberService = new MemberService(memberRepository);
    }

    @Test
    @DisplayName("사용자의 이메일, 비밀번호를 확인한 후 사용자의 아이디를 반환한다.")
    void authenticateTest() {
        // given
        memberService.addMember(new MemberRegisterRequest("test@test.com", "testPassword", "test"));
        final LoginRequest loginRequest = new LoginRequest("test@test.com", "testPassword");

        // when
        final Long memberId = authService.authenticate(loginRequest);

        // then
        final Member saved = memberService.getMemberById(memberId);
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getName()).isEqualTo("test");
    }

    @Test
    @DisplayName("사용자의 이메일을 찾을 수 없는 경우 예외가 발생한다")
    void noEmailAuthenticateTest() {
        // given
        memberService.addMember(new MemberRegisterRequest("test@test.com", "testPassword", "test"));
        final LoginRequest loginRequest = new LoginRequest("wrongEmail@test.com", "testPassword");

        // when, then
        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용자의 패스워드가 일치하지 않는 경우 예외가 발생한다.")
    void wrongPasswordAuthenticateTest() {
        // given
        memberService.addMember(new MemberRegisterRequest("test@test.com", "testPassword", "test"));
        final LoginRequest loginRequest = new LoginRequest("test@test.com", "wrongPassword");

        // when, then
        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("세션 아이디를 업데이트 한다")
    void updateSessoinId() {
        // given
        final MemberRegisterResponse response = memberService.addMember(
                new MemberRegisterRequest("test@test.com", "testPassword", "test")
        );
        final Member member = memberService.getMemberById(response.id());
        final String sessionIdBefore = member.getSessionId();

        // when
        authService.updateSessionIdByMemberId(member.getId(), "hello");
        entityManager.clear();

        // then
        final Member memberAfterChanged = memberService.getMemberById(response.id());
        assertAll(
                () -> assertThat(sessionIdBefore).isNull(),
                () -> assertThat(memberAfterChanged.getSessionId()).isEqualTo("hello")
        );
    }
}
