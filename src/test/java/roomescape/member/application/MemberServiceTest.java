package roomescape.member.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.SignUpRequest;
import roomescape.member.presentation.dto.SignUpResponse;
import roomescape.member.presentation.dto.TokenRequest;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("토큰 생성 테스트")
    void createTokenTest() {
        // given
        TokenRequest tokenRequest = new TokenRequest("admin@admin.com", "admin");

        // when
        String token = memberService.createToken(tokenRequest);

        // then
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("토큰으로 유저 조회 테스트")
    void findByTokenTest() {
        // given
        TokenRequest tokenRequest = new TokenRequest("admin@admin.com", "admin");
        String token = memberService.createToken(tokenRequest);
        token = token.replaceFirst("token=", "");

        // when
        MemberResponse memberResponse = memberService.findByToken(token);

        // then
        assertThat(memberResponse.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("id로 유저 조회 테스트")
    void getMemberByIdTest() {
        assertThat(memberService.getMemberById(2L).getName()).isEqualTo("어드민");
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest("test@test.com", "test", "테스트");

        // when
        SignUpResponse signUpResponse = memberService.signUp(signUpRequest);

        // then
        assertThat(signUpResponse.getId()).isNotNull();
    }

    @Test
    @DisplayName("유저 조회 테스트")
    void getMembersTestById() {
        assertThat(memberService.getMembers().size()).isEqualTo(2);
    }
}
