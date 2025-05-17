package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.TestConfig;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.fixture.TestFixture;

@DataJpaTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations="
})
class MemberRepositoryTest {

    private static final String EMAIL = "mint@gmail.com";

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        memberRepository.save(TestFixture.makeMember());
    }

    @Test
    void existsByEmail() {
        boolean doesExist = memberRepository.existsByEmail(EMAIL);
        assertThat(doesExist).isTrue();
    }

    @Test
    void findByMemberRole() {
        memberRepository.save(new Member("Vector", "vector@gmail.com", "password", MemberRole.USER));

        List<Member> users = memberRepository.findByMemberRole(MemberRole.USER);

        assertThat(users.size()).isEqualTo(2);
    }
}
