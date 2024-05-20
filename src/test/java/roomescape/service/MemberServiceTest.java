package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.controller.member.dto.SignupRequest;
import roomescape.domain.Member;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Test
    @DisplayName("멤버를 저장한다.")
    void save() {
        final SignupRequest request = new SignupRequest("aaa@mail.com", "qwer1234", "레디");
        final Member saved = memberService.save(request);

        assertAll(
                () -> assertThat(saved.getName()).isEqualTo("레디"),
                () -> assertThat(saved.getEmail()).isEqualTo("aaa@mail.com")
        );
    }
}
