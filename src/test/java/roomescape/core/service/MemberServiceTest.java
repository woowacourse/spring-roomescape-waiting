package roomescape.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.core.dto.auth.TokenRequest;
import roomescape.core.dto.auth.TokenResponse;
import roomescape.core.dto.member.LoginMember;
import roomescape.core.dto.member.MemberRequest;
import roomescape.core.dto.member.MemberResponse;
import roomescape.infrastructure.TokenProvider;
import roomescape.utils.DatabaseCleaner;
import roomescape.utils.TestFixture;

@ServiceTest
class MemberServiceTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private TestFixture testFixture;

    @BeforeEach
    void setUp() {
        databaseCleaner.executeTruncate();
        testFixture.initTestData();
    }

    @Test
    @DisplayName("로그인이 성공하면 토큰을 생성한다.")
    void createToken() {
        final TokenRequest request = new TokenRequest(TestFixture.getAdminEmail(), TestFixture.getPassword());

        final TokenResponse response = memberService.createToken(request);

        assertThat(tokenProvider.getPayload(response.getAccessToken())).isEqualTo(TestFixture.getAdminEmail());
    }

    @Test
    @DisplayName("토큰을 통해 회원을 조회할 수 있다.")
    void findMemberByToken() {
        final TokenRequest request = new TokenRequest(TestFixture.getAdminEmail(), TestFixture.getPassword());
        final TokenResponse tokenResponse = memberService.createToken(request);
        final String token = tokenResponse.getAccessToken();

        final MemberResponse response = memberService.findMemberByToken(token);

        assertThat(response.getName()).isEqualTo("리건");
    }

    @Test
    @DisplayName("토큰을 통해 로그인한 회원을 조회할 수 있다.")
    void findLoginMemberByToken() {
        final TokenRequest request = new TokenRequest(TestFixture.getAdminEmail(), TestFixture.getPassword());
        final TokenResponse tokenResponse = memberService.createToken(request);
        final String token = tokenResponse.getAccessToken();

        final LoginMember response = memberService.findLoginMemberByToken(token);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("모든 회원을 조회한다.")
    void findAll() {
        final List<MemberResponse> responses = memberService.findAll();

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("회원 가입을 할 수 있다.")
    void create() {
        final MemberRequest request = new MemberRequest("ligan@email.com", "1234", "리건");

        final MemberResponse response = memberService.create(request);

        assertThat(response.getName()).isEqualTo("리건");
    }

    @Test
    @DisplayName("이미 사용 중인 이메일로 회원 가입을 할 수 없다.")
    void createWithAlreadyUsedEmail() {
        final MemberRequest request = new MemberRequest(TestFixture.getAdminEmail(), TestFixture.getPassword(), "리건");

        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
    }
}