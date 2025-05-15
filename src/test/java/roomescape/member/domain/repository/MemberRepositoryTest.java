package roomescape.member.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;

@ActiveProfiles("test")
@DataJpaTest
class MemberRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("회원을 저장한다")
    @Test
    void save() {
        // given
        String name = "kim";
        String email = "kim@gmail.com";
        String password = "1234";
        Member member = new Member(name, email, password);

        // when
        memberRepository.save(member);
        Iterable<Member> members = memberRepository.findAll();

        // then
        assertThat(members).extracting(Member::getName, Member::getEmail, Member::getPassword)
                .containsExactlyInAnyOrder(tuple(name, email, password));
    }
}
