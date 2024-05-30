package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_BRI;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberPassword;

@DataJpaTest
@Sql(scripts = "/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("email과 password로 멤버를 조회할 수 있다.")
    @Test
    void findByEmailAndPasswordTest() {
        // given
        Member savedMember = memberRepository.save(MEMBER_BRI);

        // when
        Optional<Member> actual = memberRepository.findByEmailAndPassword(
                new MemberEmail(MEMBER_BRI.getEmail()),
                new MemberPassword(MEMBER_BRI.getPassword()));

        // then
        assertThat(actual).contains(savedMember);
    }
}
