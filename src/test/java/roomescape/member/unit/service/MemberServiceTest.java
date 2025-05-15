package roomescape.member.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.global.error.exception.ConflictException;
import roomescape.member.dto.request.MemberCreateRequest;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;

@DataJpaTest
class MemberServiceTest {

    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

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
        var request1 = new MemberCreateRequest("미소", "miso@email.com", "password");
        var request2 = new MemberCreateRequest("브라운", "brown@email.com", "password");
        memberService.createMember(request1);
        memberService.createMember(request2);

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
        var request = new MemberCreateRequest("미소", "miso@email.com", "password");
        var response = memberService.createMember(request);

        // when
        memberService.deleteMember(response.id());

        // then
        var members = memberService.getAllMembers();
        assertThat(members).isEmpty();
    }

    @Test
    @DisplayName("중복된 이메일로 회원을 생성할 수 없다.")
    void validateDuplicateEmail() {
        // given
        var request1 = new MemberCreateRequest("미소", "miso@email.com", "password");
        memberService.createMember(request1);

        var request2 = new MemberCreateRequest("브라운", "miso@email.com", "password");

        // when & then
        assertThatThrownBy(() -> memberService.createMember(request2))
                .isInstanceOf(ConflictException.class)
                .hasMessage("중복된 이메일입니다.");
    }
}
