package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations="
})
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member userMember;
    private Member adminMember;

    @BeforeEach
    void setUp() {
        userMember = memberRepository.save(
                new Member("vector", "usera@example.com", "passA", MemberRole.USER));
        adminMember = memberRepository.save(
                new Member("mint", "adminb@example.com", "passB", MemberRole.ADMIN));
    }

    @Test
    void findByIdTest() {
        Member newMember = new Member("NewC", "newc@example.com", "passC", MemberRole.USER);
        Member saved = memberRepository.save(newMember);

        assertThat(saved.getId()).isNotNull();

        Optional<Member> found = memberRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("newc@example.com");
    }

    @Test
    void existsByEmailIfNoMember() {
        assertThat(memberRepository.existsByEmail(userMember.getEmail())).isTrue();
        assertThat(memberRepository.existsByEmail(adminMember.getEmail())).isTrue();
        assertThat(memberRepository.existsByEmail("unknown@example.com")).isFalse();
    }

    @Test
    void findByMemberRole() {
        List<Member> users = memberRepository.findByMemberRole(MemberRole.USER);
        assertThat(users)
                .hasSize(1)
                .extracting(Member::getEmail)
                .containsExactly(userMember.getEmail());

        List<Member> admins = memberRepository.findByMemberRole(MemberRole.ADMIN);
        assertThat(admins)
                .hasSize(1)
                .extracting(Member::getEmail)
                .containsExactly(adminMember.getEmail());
    }

    @Test
    void findByIdEmptyTest() {
        Optional<Member> missing = memberRepository.findById(999L);
        assertThat(missing).isEmpty();
    }
}
