package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

@DataJpaTest
class MemberRepositoryTest {
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void setUp() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("성공 : 회원 정보를 DB에 저장할 수 있다.")
    void save() {
        // Given
        Member member = new Member("newbie", "new@memeber.com", "password");

        // When
        Member actual = memberRepository.save(member);

        // Then
        List<Member> expected = memberRepository.findAll();
        assertThat(actual).isEqualTo(expected.get(0));
    }

    @Test
    @DisplayName("성공 : DB에 있는 모든 회원 정보를 얻을 수 있다.")
    void findAll() {
        // Given
        entityManager.persist(new Member("어드민", "admin@admin.com", "1234"));
        entityManager.persist(new Member("도비", "kimdobby@wootaeco.com", "pass1"));
        entityManager.persist(new Member("켬미", "test@test.com", "test"));

        // When
        List<Member> members = memberRepository.findAll();

        // Then
        assertAll(() -> {
            assertThat(members).hasSize(3);
            assertThat(members).extracting(Member::getEmail)
                    .containsOnly("admin@admin.com", "kimdobby@wootaeco.com", "test@test.com");
        });
    }

    @Test
    @DisplayName("성공 : 이메일로 회원 정보를 얻을 수 있다.")
    void findByEmail() {
        // Given
        String checkEmail = "kyummi@email.com";
        Member expected = new Member("켬미", checkEmail, "test");
        entityManager.persist(expected);

        // When
        Member actual = memberRepository.findByEmail(checkEmail).get();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("성공 : 주어진 권한과 동일한 권한인 회원 정보를 얻을 수 있다.")
    void findAllByRole() {
        // Given
        Member admin = new Member(null, "어드민", "admin@admin.com", "1234", MemberRole.ADMIN);
        entityManager.persist(admin);
        Member member1 = new Member(null, "도비", "kimdobby@wootaeco.com", "pass1", MemberRole.USER);
        entityManager.persist(member1);
        Member member2 = new Member(null, "켬미", "test@test.com", "test", MemberRole.USER);
        entityManager.persist(member2);

        // When
        List<Member> actual = memberRepository.findAllByRole(MemberRole.USER);

        // Then
        assertThat(actual).containsExactly(member1, member2);
    }

    @Test
    @DisplayName("성공 : id로 회원 정보를 찾아 지운다.")
    void deleteById() {
        // Given
        Member expected = new Member("도비", "kimdobby@wootaeco.com", "pass1");
        entityManager.persist(expected);

        // When
        memberRepository.deleteById(expected.getId());

        // Then
        assertThat(memberRepository.findById(expected.getId())).isEmpty();
    }
}
