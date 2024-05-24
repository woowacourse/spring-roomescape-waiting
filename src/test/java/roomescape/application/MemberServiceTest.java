package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.BasicAcceptanceTest;
import roomescape.dto.MemberSignUpRequest;
import roomescape.exception.RoomescapeException;

class MemberServiceTest extends BasicAcceptanceTest {
    @Autowired
    private MemberService memberService;

    @DisplayName("이미 존재하는 이메일을 저장할 시 예외를 발생시킨다")
    @Test
    void duplicateEmail() {
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("중복", "bito@wooteco.com", "dupilcate");

        assertThatThrownBy(() -> memberService.save(memberSignUpRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage("이미 존재하는 아이디입니다.");
    }
}
