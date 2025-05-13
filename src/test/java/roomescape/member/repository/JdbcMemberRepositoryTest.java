package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

@JdbcTest
@Import(JdbcMemberRepository.class)
class JdbcMemberRepositoryTest {

    @Autowired
    private JdbcMemberRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 새로운_멤버를_저장() {
        // given
        Member member = new Member("테스터", "test@example.com", "secret");

        // when
        repository.save(member);
        Optional<Member> found = repository.findByEmail("test@example.com");

        // then
        assertThat(found).isPresent();
        Member result = found.get();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("테스터");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("secret");
        assertThat(result.getRole()).isEqualTo(MemberRole.MEMBER);
    }

    @Test
    void 저장된_멤버를_id로_조회() {
        // given
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)",
                "A", "a@a.com", "pw", "ADMIN"
        );
        Long id = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = ?", Long.class, "a@a.com");

        // when
        Optional<Member> found = repository.findById(id);

        // then
        assertThat(found).isPresent();
        Member result = found.get();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("A");
        assertThat(result.getEmail()).isEqualTo("a@a.com");
        assertThat(result.getPassword()).isEqualTo("pw");
        assertThat(result.getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void 저장된_모든_멤버_리스트_조회() {
        // given
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)",
                "X", "x@x.com", "px", "MEMBER"
        );
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)",
                "Y", "y@y.com", "py", "ADMIN"
        );

        // when
        List<Member> members = repository.findAll();

        // then - 기존 data.sql에 2개 존재
        assertThat(members).hasSize(4);
        assertThat(members).extracting(Member::getName).containsExactlyInAnyOrder("운영진", "홍길동", "X", "Y");
        assertThat(members).extracting(Member::getRole)
                .containsExactlyInAnyOrder(MemberRole.ADMIN, MemberRole.MEMBER, MemberRole.MEMBER, MemberRole.ADMIN);
    }

    @Test
    void 저장된_멤버를_이메일로_조회() {
        // given
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)",
                "B", "b@b.com", "pb", "MEMBER"
        );

        // when
        Optional<Member> found = repository.findByEmail("b@b.com");

        // then
        assertThat(found).isPresent();
        Member result = found.get();
        assertThat(result.getEmail()).isEqualTo("b@b.com");
        assertThat(result.getName()).isEqualTo("B");
    }
}
