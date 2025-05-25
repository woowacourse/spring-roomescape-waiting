package roomescape.member.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import(MemberRepositoryAdapter.class)
class MemberRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("회원을 저장한다")
    @Test
    void save() {
        // given
        String name = "에드";
        String email = "ed@example.com";
        String password = "password123";
        Member member = MemberFixture.createMember(name, email, password);

        // when
        Member savedMember = memberRepository.save(member);

        // then
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getName().getValue()).isEqualTo(name);
        assertThat(savedMember.getEmail().getValue()).isEqualTo(email);
    }

    @DisplayName("이메일로 회원을 조회한다")
    @Test
    void findByEmail() {
        // given
        String name = "에드";
        String email = "ed@example.com";
        String password = "password123";
        Member member = MemberFixture.createMember(name, email, password);
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByEmail(email);

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName().getValue()).isEqualTo(name);
        assertThat(foundMember.get().getEmail().getValue()).isEqualTo(email);
    }

    @DisplayName("이메일로 회원 존재 여부를 확인한다")
    @Test
    void existsByEmail() {
        // given
        String name = "에드";
        String email = "ed@example.com";
        String password = "password123";
        Member member = MemberFixture.createMember(name, email, password);
        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByEmail(email);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("모든 회원을 조회한다")
    @Test
    void findAll() {
        // given
        Member member1 = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member1);

        Member member2 = MemberFixture.createMember("김진우", "jinu@example.com", "password456");
        memberRepository.save(member2);

        // when
        List<Member> members = memberRepository.findAll();

        // then
        assertThat(members).hasSize(2);
    }

    @DisplayName("ID로 회원을 조회한다")
    @Test
    void findById() {
        // given
        String name = "에드";
        String email = "ed@example.com";
        String password = "password123";
        Member member = MemberFixture.createMember(name, email, password);
        Member savedMember = memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getId()).isEqualTo(savedMember.getId());
        assertThat(foundMember.get().getName().getValue()).isEqualTo(name);
        assertThat(foundMember.get().getEmail().getValue()).isEqualTo(email);
    }
}
