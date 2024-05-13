package roomescape.controller.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import roomescape.controller.member.dto.MemberLoginResponse;
import roomescape.controller.member.dto.SignupRequest;
import roomescape.service.exception.DuplicateEmailException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemberControllerTest {

    @Autowired
    MemberController memberController;

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
        final SignupRequest request = new SignupRequest("gkatjraud1@redddybabo.com", "1234", "뉴멤버");

        assertThatThrownBy(() -> memberController.createMember(request))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
