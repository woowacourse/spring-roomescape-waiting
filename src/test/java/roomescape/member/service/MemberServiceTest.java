package roomescape.member.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.domain.Member;

class MemberServiceTest extends BaseTest {

    @Autowired
    MemberService memberService;

    @Test
    @DisplayName("회원을 저장한다.")
    void saveMemberTest() {
        //given
        Member matt = memberService.save(new SignUpRequest("매트", "matt.kakao", "1234"));

        //when - then
        assertAll(
                () -> assertEquals(1L, matt.getId()),
                () -> assertEquals("매트", matt.getName()),
                () -> assertEquals("matt.kakao", matt.getEmail()),
                () -> assertEquals("1234", matt.getPassword())
        );
    }

}
