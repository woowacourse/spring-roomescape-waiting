package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Role;
import roomescape.repository.MemberRepository;
import roomescape.service.member.dto.MemberRequest;
import roomescape.service.member.dto.MemberResponse;
import roomescape.service.member.MemberService;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @DisplayName("사용자를 생성한다.")
    @Test
    void createMember() {
        MemberRequest memberRequest = new MemberRequest("jamie", "jamie@email.com", "password", Role.USER);

        MemberResponse memberResponse = memberService.createMember(memberRequest);

        assertThat(memberResponse.name()).isEqualTo(memberRequest.name());
    }

    @DisplayName("모든 사용자를 조회한다.")
    @Test
    void findAllMembers() {
        assertThat(memberService.findAllMembers()).hasSize(3);
    }
}
