package roomescape.domain.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Member;

@SpringBootTest
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원 정보를 영속화한다")
    void save_ShouldSavePersistence_WhenHasNotKey() {
        // given
        Member member = new Member(1L, "name", "aa@aa.aa", "aa");

        // when
        memberRepository.save(member);

        // then
        Assertions.assertThat(memberRepository.findAll())
                .hasSize(1);
    }

    @Test
    @DisplayName("영속화 된 회원 정보를 id로 불러온다")
    void findById_ShouldGetPersistenceById() {
        // given
        Member member = new Member("name", "aa@aa.aa", "aa");
        memberRepository.save(member);

        // when & then
        Assertions.assertThat(memberRepository.findById(member.getId()))
                .isPresent()
                .hasValue(member);
    }

    @Test
    @DisplayName("영속화 되지 않은 회원 정보를 id로 불러온다")
    void findById_ShouldGetPersistenceById_WhenPersistenceDoseNotExists() {
        // when & then
        Assertions.assertThat(memberRepository.findById(0L))
                .isEmpty();
    }

    @Test
    @DisplayName("영속화 된 회원 정보를 email과 password로 불러온다")
    void findByEmailAndPassword_ShouldGetPersistenceByEmailAndPassword() {
        // given
        Member member = new Member("name", "aa@aa.aa", "aa");
        memberRepository.save(member);

        // when & then
        Assertions.assertThat(
                        memberRepository.findByEmailAndPassword(member.getEmail(), member.getPassword()))
                .isPresent()
                .hasValue(member);
    }

    @Test
    @DisplayName("영속화 되지 않은 정보를 email과 password로 불러온다")
    void findByEmailAndPassword_ShouldGetPersistenceByEmailAndPassword_WhenPersistenceDoseNotExists() {
        // given
        Member member = new Member("name", "aa@aa.aa", "aa");

        // when & then
        Assertions.assertThat(memberRepository.findByEmailAndPassword(member.getEmail(),
                        member.getPassword()))
                .isEmpty();
    }

    @Test
    @DisplayName("영속화 된 정보인지를 email로 확인한다-존재 시")
    void existsByEmail_ShouldCheckPersistenceExists() {
        // given
        Member member = new Member("name", "aa@aa.aa", "aa");
        Member savedMember = memberRepository.save(member);

        // when & then
        Assertions.assertThat(memberRepository.existsByEmail(savedMember.getEmail()))
                .isTrue();
    }

    @Test
    @DisplayName("영속화 된 정보인지를 email로 확인한다-존재 무")
    void existsByEmail_ShouldCheckPersistenceExist_WhenPersistenceDoesNotExist() {
        //given
        Member member = new Member("name", "aa@aa.aa", "aa");

        // when & then
        Assertions.assertThat(memberRepository.existsByEmail(member.getEmail()))
                .isFalse();
    }

    @Test
    @DisplayName("영속화 된 정보를 삭제한다")
    void delete_ShouldRemovePersistenceExist() {
        // given
        Member member = new Member("name", "aa@aa.aa", "aa");
        memberRepository.save(member);

        // when
        memberRepository.delete(member);

        // when & then
        Assertions.assertThat(memberRepository.findAll())
                .isEmpty();
    }

    @Test
    @DisplayName("모든 영속화를 삭제한다")
    void delete_ShouldRemoveAllPersistence() {
        // given
        memberRepository.save(new Member("aa", "aa@aa.aa", "aa"));
        memberRepository.save(new Member("bb", "bb@aa.aa", "bb"));
        memberRepository.save(new Member("cc", "cc@aa.aa", "cc"));

        // when
        memberRepository.deleteAll();

        // then
        Assertions.assertThat(memberRepository.findAll()).isEmpty();
    }
}
