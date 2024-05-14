package roomescape.member.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.jdbc.Sql;
import roomescape.Fixtures;
import roomescape.member.domain.Member;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("사용자 DAO")
@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@JdbcTest
class MemberDaoTest {

    private final MemberRepository memberRepository;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public MemberDaoTest(JdbcTemplate jdbcTemplate) {
        this.memberRepository = new MemberDao(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @DisplayName("사용자 DAO는 생성 요청이 들어오면 DB에 값을 저장한다.")
    @Test
    void save() {
        // given
        Member member = Fixtures.memberFixture;

        // when
        Member savedMember = memberRepository.save(member);
        Optional<Member> actual = memberRepository.findById(savedMember.getId());

        // then
        assertThat(actual).isPresent();
    }

    @DisplayName("사용자 DAO는 조회 요청이 들어오면 DB에 저장된 모든 테마를 반환한다.")
    @Test
    void findAll() {
        // when
        List<Member> members = memberRepository.findAll();

        // then
        assertThat(members).hasSize(7);
    }

    @DisplayName("사용자 DAO는 email에 일치하는 사용자를 반환한다.")
    @Test
    void findUserByEmail() {
        // given
        String email = "test@gmail.com";

        // when
        Optional<Member> actual = memberRepository.findByEmail(email);

        // then
        assertThat(actual).isPresent();
    }
}
