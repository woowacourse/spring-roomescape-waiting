package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.util.Fixture.KAKI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
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

    @DisplayName("중복된 이름 또는 이메일로 회원가입할 수 없다.")
    @Test
    void save() {
        Member kaki = memberRepository.save(KAKI);

        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(kaki.getName(), kaki.getEmail(), kaki.getPassword());

        assertThatThrownBy(() -> memberService.save(memberSignUpRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
