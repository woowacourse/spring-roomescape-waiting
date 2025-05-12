package roomescape.member.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.dto.request.MemberCreateRequest;
import roomescape.member.exception.DuplicateMemberException;
import roomescape.member.model.Member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(MemberService.class)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    void 멤버를_추가한다() {
        // Given
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest("phree@woowa.com", "password", "프리");

        // When
        Member newMember = memberService.createUser(memberCreateRequest);

        // Then
        assertAll(() -> {
            assertThat(newMember).isNotNull();
            assertThat(newMember.getEmail()).isEqualTo("phree@woowa.com");
            assertThat(newMember.getPassword()).isEqualTo("password");
            assertThat(newMember.getName()).isEqualTo("프리");
        });
    }

    @Test
    void 중복된_이름을_가진_멤버는_추가할_수_없다() {
        // Given
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest("phree@woowa.com", "password", "프리");
        MemberCreateRequest memberCreateRequest2 = new MemberCreateRequest("phree2@woowa.com", "password", "프리");
        memberService.createUser(memberCreateRequest);

        // When
        // Then
        assertThatThrownBy(() -> memberService.createUser(memberCreateRequest2))
                .isInstanceOf(DuplicateMemberException.class)
                .hasMessage("이미 존재하는 이름이다.");
    }

    @Test
    void 중복된_이메일을_가진_멤버는_추가할_수_없다() {
        // Given
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest("phree@woowa.com", "password", "프리");
        MemberCreateRequest memberCreateRequest2 = new MemberCreateRequest("phree@woowa.com", "password", "프리2");
        memberService.createUser(memberCreateRequest);

        // When
        // Then
        assertThatThrownBy(() -> memberService.createUser(memberCreateRequest2))
                .isInstanceOf(DuplicateMemberException.class)
                .hasMessage("이미 가입된 이메일이다.");
    }
}