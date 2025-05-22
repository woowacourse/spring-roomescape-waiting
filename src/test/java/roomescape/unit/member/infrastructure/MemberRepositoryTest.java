package roomescape.unit.member.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.MemberRepository;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 이메일로_회원을_찾을_수_있다() {
        // given
        entityManager.persist(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        // when
        Optional<Member> member = memberRepository.findByEmail("email1@domain.com");
        // then
        assertThat(member.isPresent()).isTrue();
        assertThat(member.get().getName()).isEqualTo("name1");
    }
}