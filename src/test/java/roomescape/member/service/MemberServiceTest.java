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
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberSignUpRequest;
import roomescape.member.repository.MemberRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MemberServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @Test
    @DisplayName("중복된 이름 또는 이메일로 회원가입할 수 없다.")
    void save() {
        String name = "hogi";
        String email = "hogi@naver.com";
        String password = "asd";
        Member member = new Member(new MemberName(name), email, password);
        memberRepository.save(member);

        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(name, email, password);

        assertThatThrownBy(() -> memberService.save(memberSignUpRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    @DisplayName("로그인에 성공하면 토큰을 발급한다.")
    void createMemberToken() {
        String name = "hogi";
        String email = "hogi@naver.com";
        String password = "asd";
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(name, email, password);
        memberService.save(memberSignUpRequest);
        MemberLoginRequest memberLoginRequest = new MemberLoginRequest(email, password);
        String memberToken = memberService.createMemberToken(memberLoginRequest);

        assertThat(memberToken).isNotNull();
    }
}
