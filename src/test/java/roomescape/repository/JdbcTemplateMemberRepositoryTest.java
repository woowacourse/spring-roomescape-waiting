package roomescape.repository;

import static roomescape.fixture.MemberFixture.DEFAULT_ADMIN;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Member;

@SpringBootTest
class JdbcTemplateMemberRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void init() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    @DisplayName("이메일과 암호화된 비밀번호로 회원을 잘 조회하는지 확인")
    void findByEmailAndEncryptedPassword() {
        String email = DEFAULT_MEMBER.getEmail();
        String encryptedPassword = DEFAULT_MEMBER.getEncryptedPassword();
        Member member = memberRepository.findByEmailAndEncryptedPassword(email, encryptedPassword)
                .orElseThrow();

        Assertions.assertThat(member)
                .isEqualTo(DEFAULT_MEMBER);
    }

    @Test
    @DisplayName("회원 아이디로 회원을 잘 조회하는지 확인")
    void findById() {
        Member member = memberRepository.findById(DEFAULT_MEMBER.getId()).orElseThrow();

        Assertions.assertThat(member)
                .isEqualTo(DEFAULT_MEMBER);
    }

    @Test
    @DisplayName("전체 회원을 잘 조회하는지 확인")
    void findAll() {
        List<Member> all = memberRepository.findAll();

        Assertions.assertThat(all)
                .containsExactlyInAnyOrder(DEFAULT_MEMBER, DEFAULT_ADMIN);
    }
}
