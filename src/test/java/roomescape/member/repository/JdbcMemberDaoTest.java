package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;

@JdbcTest
@Import(JdbcMemberDao.class)
@Sql({"/test-schema.sql", "/test-member-data.sql"})
class JdbcMemberDaoTest {

    @Autowired
    private JdbcMemberDao jdbcMemberDao;

    @DisplayName("사용자를 저장할 수 있다.")
    @Test
    void testSave() {
        // given
        MemberName name = new MemberName("이름");
        MemberEmail email = new MemberEmail("aaa@bbb.com");
        String password = "1234";
        Member member = Member.register(name, email, password);
        // when
        Member saveMember = jdbcMemberDao.save(member);
        // then
        assertAll(
                () -> assertThat(saveMember.getId()).isEqualTo(4L),
                () -> assertThat(jdbcMemberDao.findById(4L).orElseThrow().getMemberName()).isEqualTo(name)
        );
    }

    @DisplayName("모든 사용자를 불러올 수 있다.")
    @Test
    void testFindAll() {
        // when
        List<Member> actual = jdbcMemberDao.findAll();
        // then
        assertThat(actual).hasSize(3);
    }

    @DisplayName("이메일과 비밀번호가 일치하는 사용자를 찾을 수 있다.")
    @Test
    void testFindByEmailAndPassword() {
        // given
        MemberEmail email = new MemberEmail("aaa@gmail.com");
        String password = "1234";
        // when
        Member actual = jdbcMemberDao.findByEmailAndPassword(email, password).orElseThrow();
        // then
        assertAll(
                () -> assertThat(actual.getId()).isEqualTo(1L),
                () -> assertThat(actual.getMemberName()).isEqualTo(new MemberName("사용자1"))
        );
    }

    @DisplayName("ID로 특정 사용자를 불러올 수 있다.")
    @Test
    void testFindById() {
        // when
        Member actual = jdbcMemberDao.findById(1L).orElseThrow();
        // then
        assertAll(
                () -> assertThat(actual.getId()).isEqualTo(1L),
                () -> assertThat(actual.getMemberName()).isEqualTo(new MemberName("사용자1"))
        );
    }

    @DisplayName("특정 이메일이 존재하는지 확인할 수 있다.")
    @Test
    void testExistByEmail() {
        // when
        boolean actual = jdbcMemberDao.existsByEmail(new MemberEmail("aaa@gmail.com"));
        // then
        assertThat(actual).isTrue();
    }

    @DisplayName("특정 이름이 존재하는지 확인할 수 있다.")
    @Test
    void testExistByName() {
        // when
        boolean actual = jdbcMemberDao.existsByName(new MemberName("사용자1"));
        // then
        assertThat(actual).isTrue();
    }
}
