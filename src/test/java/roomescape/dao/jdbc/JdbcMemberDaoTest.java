package roomescape.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Member;
import roomescape.exception.custom.NotFoundException;

@JdbcTest
@Import(JdbcMemberDao.class)
class JdbcMemberDaoTest {

    @Autowired
    private JdbcMemberDao jdbcMemberDao;

    @Test
    @DisplayName("이메일이 존재한다면 조회할 수 있다.")
    void findMemberByExistedEmail() {
        String email = "sa123";
        Member expected = jdbcMemberDao.findMemberByEmail(email);

        assertThat(expected.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("이메일이 존재하지 않는다면 예외가 발생한다.")
    void findMemberByNotExistedEmail() {
        assertThatThrownBy(() -> jdbcMemberDao.findMemberByEmail("notEmail"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("member");
    }

    @Test
    @DisplayName("사용자 ID가 존재한다면 조회할 수 있다.")
    void findMemberByExistedId() {
        Long id = 1L;
        Member expected = jdbcMemberDao.findMemberById(id);

        assertThat(expected.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("사용자 ID가 존재하지 않는다면 예외가 발생한다.")
    void findMemberByNotExistedId() {
        assertThatThrownBy(() -> jdbcMemberDao.findMemberById(100L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("member");
    }

    @Test
    @DisplayName("해당 이메일이 없다면 true를 반환한다.")
    void existMemberByEmail() {
        Member member = new Member("이름", "이메일", "비밀번호");
        jdbcMemberDao.addMember(member);

        assertThat(jdbcMemberDao.existMemberByEmail(member.getEmail())).isTrue();
    }

    @Test
    @DisplayName("해당 이메일이 없다면 false를 반환한다.")
    void notExistMemberByEmail() {
        Member member = new Member("이름", "이메일", "비밀번호");

        assertThat(jdbcMemberDao.existMemberByEmail(member.getEmail())).isFalse();
    }

    @Test
    @DisplayName("사용자를 추가할 수 있다.")
    void addMember() {
        Member member = new Member("이름", "이메일", "비밀번호");
        Member newMember = jdbcMemberDao.addMember(member);

        assertThat(newMember).isNotNull();
    }
}
