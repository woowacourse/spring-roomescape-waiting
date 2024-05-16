package roomescape.infrastructure.role;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;
import roomescape.domain.role.RoleRepository;

@DataJpaTest
class JpaRoleRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("멤버 id로 어드민 여부를 조회한다.")
    void isAdminByMemberIdTest() {
        Member member = new Member("name", "email@test.com", "12341234");
        Member savedMember = memberRepository.save(member);
        entityManager.persist(new MemberRole(savedMember, Role.ADMIN));
        boolean isAdmin = roleRepository.isAdminByMemberId(savedMember.getId());
        assertThat(isAdmin).isTrue();
    }
}
