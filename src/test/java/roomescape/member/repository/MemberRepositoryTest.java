package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.fixture.TestFixture;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.sql.init.data-locations="
})
class MemberRepositoryTest {

    private static final String EMAIL = "mint@gmail.com";
    private static final String PASSWORD = "password";

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        memberRepository.save(TestFixture.makeMember());
    }

    @Test
    void existsByEmailAndPassword() {
        boolean doesExist = memberRepository.existsByEmailAndPassword(EMAIL, PASSWORD);
        assertThat(doesExist).isTrue();
    }

    @Test
    void findMemberByEmailAndPassword() {
        Optional<Member> member = memberRepository.findByEmailAndPassword(EMAIL, PASSWORD);

        assertThat(member.get().getEmail()).isEqualTo(EMAIL);
    }
}
