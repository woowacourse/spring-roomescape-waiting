package roomescape.member.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.business.domain.Member;
import roomescape.member.business.domain.Role;
import roomescape.member.business.repository.MemberDao;

@JdbcTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcMemberDaoTest {

    private MemberDao memberDao;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        memberDao = new JdbcMemberDao(jdbcTemplate);
    }

    @Test
    void 이메일과_비밀번호로_회원을_조회한다() {
        // given
        final String email = "test1@test.com";
        final String password = "1234";
        // when

        // then
        assertThat(memberDao.findByEmailAndPassword(email, password).get()).isEqualTo(
                new Member(1L, "", "", "", Role.MEMBER)
        );
    }

    @Test
    void 멤버를_저장한다() {
        // given
        final Member member = new Member("파랑", "test4@test.com", "1234", Role.MEMBER);

        // when
        final Member saved = memberDao.save(member);
        final Member expected = new Member(5L, "파랑", "test4@test.com", "1234", Role.MEMBER);

        // then
        assertThat(saved).isEqualTo(expected);
    }

    @Test
    void 멤버를_제거한다() {
        // given
        final Member member = new Member("파랑", "test4@test.com", "1234", Role.MEMBER);
        memberDao.save(member);

        // when
        memberDao.deleteById(5L);

        // then
        assertThat(memberDao.findAll()).hasSize(4);
    }

    @Test
    void id로_멤버를_조회한다() {
        // given
        final Member expected = new Member(1L, "엠제이", "test1@test.com", "1234", Role.MEMBER);

        // when & then
        assertThat(memberDao.findById(1L).get())
                .isEqualTo(expected);
    }

    @Test
    void 이메일과_비밀번호로_멤버를_조회한다() {
        // given
        final String email = "test1@test.com";
        final String password = "1234";

        // when & then
        assertThat(memberDao.findByEmailAndPassword(email, password))
                .isEqualTo(Optional.of(new Member(1L, "엠제이", "test1@test.com", "1234", Role.MEMBER))
                );
    }

    @Test
    void 이메일로_사용자가_존재하는지_조회한다() {
        // given
        final String email = "test1@test.com";

        // when & then
        assertThat(memberDao.existsByEmail(email)).isTrue();
    }
}
