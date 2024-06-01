package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.auth.dto.SignUpRequest;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.member.service.MemberService;

@Import(MemberService.class)
class MemberServiceTest extends ServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원 추가 기능 수행 시, Email이 이미 등록되어 있으면 예외를 발생시킨다.")
    void failAddMemberByEmailDuplicate() {
        String email = "eden@eden.com";
        memberFixture.createMember(email);

        Assertions.assertThatThrownBy(() ->
                        memberService.addMember(new SignUpRequest("이름2", email, "12341234")))
                .isInstanceOf(DataDuplicateException.class);
    }
}
