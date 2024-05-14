package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

@JdbcTest
class JdbcMemberRepositoryTest {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcMemberRepository memberRepository;

    @Autowired
    public JdbcMemberRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberRepository = new JdbcMemberRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("회원을 추가한다.")
    void save() {
        Member member = new Member("example@gmail.com", "password", "구름", Role.USER);
        Member savedMember = memberRepository.save(member);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(savedMember.getId()).isNotNull();
            softly.assertThat(savedMember.getEmail()).isEqualTo("example@gmail.com");
            softly.assertThat(savedMember.getPassword()).isEqualTo("password");
            softly.assertThat(savedMember.getName()).isEqualTo("구름");
            softly.assertThat(savedMember.getRole()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("모든 회원들을 조회한다.")
    void findAll() {
        jdbcTemplate.update("INSERT INTO member (email, password, name, role) "
                + "VALUES ('example@gmail.com', 'password', '구름', 'USER')");

        List<Member> members = memberRepository.findAll();
        Member member = members.get(0);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(members).hasSize(1);
            softly.assertThat(member.getId()).isNotNull();
            softly.assertThat(member.getEmail()).isEqualTo("example@gmail.com");
            softly.assertThat(member.getPassword()).isEqualTo("password");
            softly.assertThat(member.getName()).isEqualTo("구름");
            softly.assertThat(member.getRole()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("회원을 조회한다.")
    void findById() {
        jdbcTemplate.update("INSERT INTO member (id, email, password, name, role) "
                + "VALUES (1L, 'example@gmail.com', 'password', '구름', 'USER')");

        Optional<Member> memberOptional = memberRepository.findById(1L);

        assertThat(memberOptional).isPresent();
        Member member = memberOptional.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(member.getId()).isEqualTo(1L);
            softly.assertThat(member.getEmail()).isEqualTo("example@gmail.com");
            softly.assertThat(member.getPassword()).isEqualTo("password");
            softly.assertThat(member.getName()).isEqualTo("구름");
            softly.assertThat(member.getRole()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("이메일로 회원을 조회한다.")
    void findByEmail() {
        jdbcTemplate.update("INSERT INTO member (email, password, name, role) "
                + "VALUES ('example@gmail.com', 'password', '구름', 'USER')");

        Optional<Member> memberOptional = memberRepository.findByEmail("example@gmail.com");

        assertThat(memberOptional).isPresent();
        Member member = memberOptional.get();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(member.getId()).isNotNull();
            softly.assertThat(member.getEmail()).isEqualTo("example@gmail.com");
            softly.assertThat(member.getPassword()).isEqualTo("password");
            softly.assertThat(member.getName()).isEqualTo("구름");
            softly.assertThat(member.getRole()).isEqualTo(Role.USER);
        });
    }

    @Test
    @DisplayName("이메일에 해당하는 회원이 존재하는지 확인한다.")
    void existsByEmail() {
        jdbcTemplate.update("INSERT INTO member (email, password, name, role) "
                + "VALUES ('example@gmail.com', 'password', '구름', 'USER')");

        assertThat(memberRepository.existsByEmail("example@gmail.com")).isTrue();
        assertThat(memberRepository.existsByEmail("nothing@gmail.com")).isFalse();
    }
}
