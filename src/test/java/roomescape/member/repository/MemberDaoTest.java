package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.utils.JdbcTemplateUtils;

@JdbcTest
@Import(MemberDao.class)
class MemberDaoTest {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private MemberDao memberDao;

    @AfterEach
    void tearDown() {
        JdbcTemplateUtils.deleteAllTables(jdbcTemplate);
    }

    @DisplayName("사용자를 DB에 저장한다.")
    @Test
    void saveMember() {
        Member member = new Member("이프", "if@posty.com", "12345678", Role.MEMBER);

        Member insertedMember = memberDao.save(member);

        assertAll(
                () -> assertThat(insertedMember.getId()).isNotZero(),
                () -> assertThat(insertedMember.getName()).isEqualTo("이프"),
                () -> assertThat(insertedMember.getEmail()).isEqualTo("if@posty.com"),
                () -> assertThat(insertedMember.getPassword()).isEqualTo("12345678")
        );
    }

    @DisplayName("이메일과 패스워드가 일치하는 사용자를 조회한다.")
    @Test
    void findMemberByEmailAndPassword() {
        String email = "if@posty.com";
        String password = "12345678";
        Member member = new Member("이프", email, password, Role.MEMBER);
        memberDao.save(member);

        Member findMember = memberDao.findByEmailAndPassword(email, password).get();

        assertAll(
                () -> assertThat(findMember.getEmail()).isEqualTo(email),
                () -> assertThat(findMember.getPassword()).isEqualTo(password)
        );
    }

    @DisplayName("이메일과 패스워드가 모두 일치하지 않으면 사용자를 조회할 수 없다.")
    @CsvSource(value = {"if@if.com:12345678", "if@posty.com:1234567"}, delimiter = ':')
    @ParameterizedTest
    void findMemberByUnmatchedEmailAndPassword(String unmatchedEmail, String unmatchedPassword) {
        String email = "if@posty.com";
        String password = "12345678";
        Member member = new Member("이프", email, password, Role.MEMBER);
        memberDao.save(member);

        Optional<Member> findMember = memberDao.findByEmailAndPassword(unmatchedEmail, unmatchedPassword);

        assertThat(findMember).isEmpty();
    }
}
