package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.member.Member;
import roomescape.model.member.MemberEmail;
import roomescape.model.member.MemberPassword;
import roomescape.model.member.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberRepositoryTest {

    private static final Member validMember = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("특정 이메일과 비밀번호를 가진 사용자를 조회한다.")
    @Test
    void should_find_member_by_email_and_password() {
        MemberEmail email = new MemberEmail(validMember.getEmail());
        MemberPassword password = new MemberPassword(validMember.getPassword());
        Optional<Member> actual = memberRepository.findByEmailAndPassword(email, password);

        assertAll(
                () -> assertThat(actual).isNotEmpty(),
                () -> assertThat(actual).hasValue(validMember));
    }

    @DisplayName("특정 이메일과 비밀번호를 가진 사용자가 존재하지 않는 경우 빈 Optional을 반환한다.")
    @Test
    void should_return_empty_optional_when_wrong_email_and_password() {
        MemberEmail email = new MemberEmail(validMember.getEmail());
        MemberPassword password = new MemberPassword(validMember.getPassword() + "wrong");
        Optional<Member> actual = memberRepository.findByEmailAndPassword(email, password);

        assertThat(actual).isEmpty();
    }
}