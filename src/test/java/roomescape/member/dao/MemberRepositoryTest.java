package roomescape.member.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;

@DataJpaTest
class MemberRepositoryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("완전한 회원정보를 정상적으로 저장한다.")
    void save_ShouldRegisterSignUpData_WhenMemberRegisterInfoGiven() {
        // Given
        Member member = new Member("newbie", "new@memeber.com", "password");

        // When
        Member actual = memberRepository.save(member);

        // Then
        List<Member> expected = memberRepository.findAll();
        assertThat(actual).isEqualTo(expected.iterator()
                .next());
    }

    @Test
    @DisplayName("데이터 베이스에 저장되어있는 모든 회원 정보를 반환한다.")
    void findAll_ShouldReturnAllRegistration_WhenCalled() {
        // Given
        entityManager.merge(new Member("어드민", "admin@admin.com", "1234"));
        entityManager.merge(new Member("도비", "kimdobby@wootaeco.com", "pass1"));
        entityManager.merge(new Member("켬미", "test@test.com", "test"));

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
    @DisplayName("회원 가입 정보를 이메일로 검색하여 반환한다.")
    void findRegistrationInfoByEmail_ShouldReturnRegistration_WhenFindByEmail() {
        // Given
        String email = "kyummi@email.com";

        entityManager.merge(new Member("어드민", "admin@admin.com", "1234"));
        entityManager.merge(new Member("도비", "kimdobby@wootaeco.com", "pass1"));
        Member expected = entityManager.merge(new Member("켬미", email, "test"));

        // When
        Member registrationInfo = memberRepository.findByEmail(email)
                .get();

        // Then
        assertThat(registrationInfo).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하는 특정 id를 가진 회원 정보를 정상적으로 지운다.")
    void delete_ShouldDeleteMemberData_WhenCalledById() {
        // Given & When
        Member targetMember = entityManager.merge(new Member("도비", "kimdobby@wootaeco.com", "pass1"));
        memberRepository.deleteById(targetMember.getId());

        // Then
        assertThat(memberRepository.findById(targetMember.getId())).isEmpty();
    }

}
