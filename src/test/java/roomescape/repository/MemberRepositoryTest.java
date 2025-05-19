package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원을 저장하고 ID로 조회할 수 있다")
    void saveAndFindById() {
        // given
        Member member = Member.createWithoutId("사용자", "user@example.com", Role.USER, "password");

        // when
        Member savedMember = memberRepository.save(member);
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("사용자");
        assertThat(foundMember.get().getEmail()).isEqualTo("user@example.com");
        assertThat(foundMember.get().getRole()).isEqualTo(Role.USER);
        assertThat(foundMember.get().getPassword()).isEqualTo("password");
    }

    @Test
    @DisplayName("모든 회원을 조회할 수 있다")
    void findAll() {
        // given
        Member member1 = Member.createWithoutId("사용자1", "user1@example.com", Role.USER, "password1");
        Member member2 = Member.createWithoutId("사용자2", "user2@example.com", Role.ADMIN, "password2");
        memberRepository.saveAll(List.of(member1, member2));

        // when
        List<Member> members = memberRepository.findAll();

        // then
        assertThat(members).hasSize(2);
        assertThat(members).extracting("name").containsExactlyInAnyOrder("사용자1", "사용자2");
        assertThat(members).extracting("email").containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    @DisplayName("회원을 삭제할 수 있다")
    void delete() {
        // given
        Member member = Member.createWithoutId("사용자", "user@example.com", Role.USER, "password");
        Member savedMember = memberRepository.save(member);

        // when
        memberRepository.delete(savedMember);
        Optional<Member> foundMember = memberRepository.findById(savedMember.getId());

        // then
        assertThat(foundMember).isEmpty();
    }

    @Test
    @DisplayName("이메일과 비밀번호로 회원을 조회할 수 있다")
    void findByEmailAndPassword() {
        // given
        Member member = Member.createWithoutId("사용자", "user@example.com", Role.USER, "password");
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByEmailAndPassword("user@example.com", "password");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("사용자");
        assertThat(foundMember.get().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("이메일로 회원 존재 여부를 확인할 수 있다")
    void existsByEmail() {
        // given
        Member member = Member.createWithoutId("사용자", "user@example.com", Role.USER, "password");
        memberRepository.save(member);

        // when
        boolean exists = memberRepository.existsByEmail("user@example.com");
        boolean notExists = memberRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
