package roomescape.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.dto.SaveMemberRequest;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(value = {"/schema.sql", "/data.sql"}, executionPhase = BEFORE_TEST_METHOD)
class MemberServiceTest {
    
    @Autowired
    private MemberService memberService;
    @Autowired
    private SslAutoConfiguration sslAutoConfiguration;

    @DisplayName("모든 회원 정보를 조회한다.")
    @Test
    void getMembersTest() {
        // When
        final List<Member> members = memberService.getMembers();

        // Then
        assertThat(members.size()).isEqualTo(5);
    }
    
    @DisplayName("회원 정보를 저장한다.")
    @Test
    void saveMemberTest() {
        // Given
        final MemberRole role = MemberRole.USER;
        final String password = "kellyPw1234";
        final String email = "kelly6bf@gmail.com";
        final String name = "kelly";
        final SaveMemberRequest saveMemberRequest = new SaveMemberRequest(email, password, name, role);

        // When
        final Member savedMember = memberService.saveMember(saveMemberRequest);

        // Then
        assertAll(
                () -> assertThat(savedMember.getId()).isEqualTo(6L),
                () -> assertThat(savedMember.getEmail().value()).isEqualTo(email),
                () -> assertThat(savedMember.getEmail().value()).isEqualTo(email),
                () -> assertThat(savedMember.getRole()).isEqualTo(role),
                () -> assertThat(savedMember.getName().value()).isEqualTo(name)
        );
    }

    @DisplayName("유효하지 않은 형식의 비밀번호가 입력되면 예외를 발생시킨다.")
    @NullAndEmptySource
    @ParameterizedTest
    void validatePlainPasswordFormatTest(final String password) {
        // Given
        final MemberRole role = MemberRole.USER;
        final String email = "kelly6bf@gmail.com";
        final String name = "kelly";
        final SaveMemberRequest saveMemberRequest = new SaveMemberRequest(email, password, name, role);

        // When & Then
        assertThatThrownBy(() -> memberService.saveMember(saveMemberRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 비밀번호로 공백을 입력할 수 없습니다.");
    }

    @DisplayName("유효하지 않은 길이의 비밀번호가 입력되면 예외를 발생시킨다.")
    @ValueSource(strings = {"aabbccdde", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @ParameterizedTest
    void validatePlainPasswordLengthTest(final String password) {
        // Given
        final MemberRole role = MemberRole.USER;
        final String email = "kelly6bf@gmail.com";
        final String name = "kelly";
        final SaveMemberRequest saveMemberRequest = new SaveMemberRequest(email, password, name, role);

        // When & Then
        assertThatThrownBy(() -> memberService.saveMember(saveMemberRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 비밀번호 길이는 10이상 30이하여만 합니다.");
    }

    @DisplayName("이미 존재하는 이메일로 회원가입 요청이 들어오면 예외를 발생시킨다.")
    @Test
    void duplicateEmailTest() {
        // Given
        final String email = "user@mail.com";
        final String password = "userPw1234!";
        final MemberRole role = MemberRole.USER;
        final String name = "kelly";
        final SaveMemberRequest saveMemberRequest = new SaveMemberRequest(email, password, name, role);

        // When & Then
        assertThatThrownBy(() -> memberService.saveMember(saveMemberRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
}
