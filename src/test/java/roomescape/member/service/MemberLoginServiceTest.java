package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberSignUpRequest;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MemberLoginServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberLoginService memberLoginService;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @DisplayName("로그인에 성공하면 토큰을 발급한다.")
    @Test
    void createMemberToken() {
        String name = "카키";
        String email = "kaki@email.com";
        String password = "1234";

        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(name, email, password);
        memberService.save(memberSignUpRequest);

        MemberLoginRequest memberLoginRequest = new MemberLoginRequest(email, password);
        String memberToken = memberLoginService.createMemberToken(memberLoginRequest);

        assertThat(memberToken).isNotNull();
    }

    @DisplayName("이메일과 비밀번호가 일치하는 회원을 조회한다.")
    @Test
    void findByEmailAndPasswordExceptionTest() {
        String name = "카키";
        String email = "kaki@email.com";
        String password = "1234";

        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(name, email, password);
        memberService.save(memberSignUpRequest);

        MemberLoginRequest memberLoginRequest = new MemberLoginRequest(email, "abcd");

        assertThatThrownBy(() -> memberLoginService.findByEmailAndPassword(memberLoginRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
