package roomescape.controller.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.IntegrationTestSupport;
import roomescape.controller.member.dto.MemberLoginResponse;
import roomescape.controller.member.dto.SignupRequest;
import roomescape.service.exception.DuplicateEmailException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.USER_EMAIL;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberControllerTest extends IntegrationTestSupport {

    @Autowired
    MemberController memberController;

    @Test
    @DisplayName("회원 조회")
    void findMembers() {
        final List<MemberLoginResponse> members = memberController.getMembers();
        assertThat(members).hasSize(3);
    }

    @Test
    @DisplayName("회원 가입")
    void createMember() {
        //given
        final SignupRequest request = new SignupRequest("new@mail.com", "486", "zz");
        final MemberLoginResponse expected = new MemberLoginResponse(4L, "zz");

        //when
        final ResponseEntity<MemberLoginResponse> response = memberController.createMember(request);

        //then
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    @DisplayName("이미 존재하는 email로 회원가입을 시도할 경우 예외가 발생")
    void duplicateEmail() {
        final SignupRequest request = new SignupRequest(USER_EMAIL, "password", "뉴멤버");

        assertThatThrownBy(() -> memberController.createMember(request))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
