package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import static roomescape.model.Role.MEMBER;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import roomescape.model.Member;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = "/test_data.sql")
class MemberRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;


    @DisplayName("아이디와 비밀번호로 사용자를 조회한다.")
    @Test
    void should_find_user_when_given_email_and_password() {
        Member member = new Member("썬", MEMBER, "sun@email.com", "1234");
        entityManager.persist(member);

        Optional<Member> findMember = memberRepository.findByEmailAndPassword("sun@email.com", "1234");

        assertThat(findMember).contains(member);
    }

    @DisplayName("아이디로 사용자를 조회한다.")
    @Test
    void should_find_user_when_given_user_id() {
        Member member = new Member("썬", MEMBER, "sun@email.com", "1234");
        entityManager.persist(member);

        Optional<Member> userById = memberRepository.findById(1L);

        assertThat(userById).contains(member);
    }

    @DisplayName("모든 사용자를 조회한다.")
    @Test
    void should_find_all_user() {
        Member member1 = new Member("무빈", MEMBER, "movin@email.com", "1111");
        Member member2 = new Member("배키", MEMBER, "dmsgml@email.com", "2222");
        entityManager.persist(member1);
        entityManager.persist(member2);

        List<Member> allUsers = memberRepository.findAll();

        assertThat(allUsers).contains(member1, member2);
    }
}
