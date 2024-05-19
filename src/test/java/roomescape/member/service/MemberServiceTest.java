package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialMemberFixture.COMMON_PASSWORD;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialMemberFixture.MEMBER_3;
import static roomescape.InitialMemberFixture.MEMBER_4;

import java.util.ArrayList;
import java.util.List;
import javax.naming.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.login.dto.LoginRequest;
import roomescape.login.service.LoginService;
import roomescape.member.dto.MemberIdNameResponse;
import roomescape.member.dto.MemberNameResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private LoginService loginService;

    @Test
    @DisplayName("모든 Member들의 id와 name을 조회한다.")
    void findMembersIdAndName() {
        List<MemberIdNameResponse> expected = new ArrayList<>();
        expected.add(new MemberIdNameResponse(MEMBER_1));
        expected.add(new MemberIdNameResponse(MEMBER_2));
        expected.add(new MemberIdNameResponse(MEMBER_3));
        expected.add(new MemberIdNameResponse(MEMBER_4));

        List<MemberIdNameResponse> found = memberService.findMembersIdAndName();

        assertThat(found).isEqualTo(expected);
    }

    @Test
    @DisplayName("유효하지 않은 형식의 토큰으로 로그인 시도 시 예외가 발생한다.")
    void throwExceptionIfInvalidTokenFormat() {
        assertThatThrownBy(() -> memberService.getMemberNameResponseByToken("invalid token"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("토큰에 대응하는 멤버 정보를 가져온다.")
    void getMemberMember() throws AuthenticationException {
        LoginRequest loginRequest = new LoginRequest(COMMON_PASSWORD.password(), MEMBER_4.getEmail().email());
        String token = loginService.createMemberToken(loginRequest);

        MemberNameResponse memberNameResponse = memberService.getMemberNameResponseByToken(token);

        assertThat(memberNameResponse.name()).isEqualTo(MEMBER_4.getName().name());
    }
}
