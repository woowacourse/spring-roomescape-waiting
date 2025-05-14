package roomescape.member.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.dto.request.MemberCreateRequest;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원을 생성한다.")
    void createMember() {
        // given
        var request = new MemberCreateRequest("미소", "miso@email.com", "password");

        // when
        var response = memberService.createMember(request);

        // then
        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.name()).isEqualTo("미소"),
                () -> assertThat(response.email()).isEqualTo("miso@email.com"),
                () -> assertThat(response.password()).isEqualTo("password"),
                () -> assertThat(response.role()).isEqualTo(RoleType.USER)
        );
    }

    @Test
    @DisplayName("모든 회원을 조회한다.")
    void getAllMembers() {
        // given
        var member1 = new Member("미소", "miso@email.com", "password", RoleType.USER);
        var member2 = new Member("브라운", "brown@email.com", "password", RoleType.USER);
        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        var responses = memberService.getAllMembers();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.get(0).name()).isEqualTo("미소"),
                () -> assertThat(responses.get(1).name()).isEqualTo("브라운")
        );
    }

    @Test
    @DisplayName("회원을 삭제한다.")
    void deleteMember() {
        // given
        var member = new Member("미소", "miso@email.com", "password", RoleType.USER);
        var savedMember = memberRepository.save(member);

        // when
        memberService.deleteMember(savedMember.getId());

        // then
        var members = memberService.getAllMembers();
        assertThat(members).isEmpty();
    }
} 
