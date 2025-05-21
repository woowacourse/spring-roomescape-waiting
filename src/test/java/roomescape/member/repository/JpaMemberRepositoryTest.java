package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.common.config.TestConfig;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.domain.MemberRole;

@DataJpaTest
@Import(TestConfig.class)
class JpaMemberRepositoryTest {

    private static Member MEMBER;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        MEMBER = TestFixture.makeMember();
        memberRepository.save(MEMBER);
    }

    @Test
    void existsByEmail_memberExist_true() {
        boolean doesExist = memberRepository.existsByEmail(MEMBER.getEmail());
        assertThat(doesExist).isTrue();
    }

    @Test
    void existsByEmail_memberDoesntExist_false() {
        // given
        String testEmail = "test@gmail.com";

        // when
        boolean doesExist = memberRepository.existsByEmail(testEmail);

        // then
        assertThat(doesExist).isFalse();
    }

    @Test
    void findByMemberRole_memberExist_one() {
        List<Member> members = memberRepository.findByMemberRole(MemberRole.REGULAR);

        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    void findByMemberRole_memberDoesntExist_zero() {
        List<Member> members = memberRepository.findByMemberRole(MemberRole.ADMIN);

        assertThat(members.size()).isEqualTo(0);
    }
}
