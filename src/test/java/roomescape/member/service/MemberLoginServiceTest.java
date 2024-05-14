package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;

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
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(KAKI_NAME, KAKI_EMAIL, KAKI_PASSWORD);
        memberService.save(memberSignUpRequest);

        MemberLoginRequest memberLoginRequest = new MemberLoginRequest(KAKI_EMAIL, KAKI_PASSWORD);
        String memberToken = memberLoginService.createMemberToken(memberLoginRequest);

        assertThat(memberToken).isNotNull();
    }

    @DisplayName("이메일과 비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void findByEmailAndPasswordExceptionTest() {
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(KAKI_NAME, KAKI_EMAIL, KAKI_PASSWORD);
        memberService.save(memberSignUpRequest);

        MemberLoginRequest memberLoginRequest = new MemberLoginRequest(KAKI_EMAIL, "abcd");

        assertThatThrownBy(() -> memberLoginService.findByEmailAndPassword(memberLoginRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
