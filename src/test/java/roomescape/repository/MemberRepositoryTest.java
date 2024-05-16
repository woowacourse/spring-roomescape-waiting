package roomescape.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.member.Email;
import roomescape.model.member.Member;
import roomescape.model.member.Password;
import roomescape.model.member.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("특정 이메일과 비밀번호를 가진 사용자를 조회한다.")
    @Test
    void should_find_member_by_email_and_password() {
        Member expected = new Member(1L, "에버", "treeboss@gmail.com", "treeboss123!", Role.USER);

        Email email = new Email(expected.getEmail());
        Password password = new Password(expected.getPassword());
        Optional<Member> actual = memberRepository.findByEmailAndPassword(email, password);

        assertAll(
                () -> assertThat(actual).isNotEmpty(),
                () -> assertThat(actual).hasValue(expected));
    }
}