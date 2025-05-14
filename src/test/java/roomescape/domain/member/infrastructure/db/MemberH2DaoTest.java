package roomescape.domain.member.infrastructure.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.member.model.Member;
import roomescape.domain.member.model.Role;
import roomescape.support.JdbcTestSupport;

@Import(MemberH2Dao.class)
class MemberH2DaoTest  extends JdbcTestSupport {

    @Autowired
    private MemberH2Dao memberH2Dao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO member (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)",
                1L, "브라운", "admin@example.com", "adminPassword", "ADMIN");

        jdbcTemplate.update("INSERT INTO member (id, name, email, password, role) VALUES (?, ?, ?, ?, ?)",
                2L, "웨이드", "user@example.com", "userPassword", "USER");
    }

    @Test
    @DisplayName("정확한 이메일과 비밀번호로 회원 조회에 성공한다")
    void selectByEmailAndPassword_success() {
        // given
        String email = "admin@example.com";
        String password = "adminPassword";

        // when
        Optional<Member> result = memberH2Dao.selectByEmailAndPassword(email, password);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("브라운");
        assertThat(result.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 조회 시 빈 결과가 반환된다")
    void selectByEmailAndPassword_wrongPassword() {
        // given
        String email = "admin@example.com";
        String wrongPassword = "not";

        // when
        Optional<Member> result = memberH2Dao.selectByEmailAndPassword(email, wrongPassword);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("모든 회원 정보를 조회할 수 있다")
    void getAll_success() {
        // given & when
        List<Member> result = memberH2Dao.getAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("브라운");
        assertThat(result.get(1).getName()).isEqualTo("웨이드");
    }
}
