package roomescape.member.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

@DataJpaTest
@Sql(scripts = "/data-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("일치하는 멤버를 가져온다.")
    void shouldThrowException_WhenIdNotFound() {
        Optional<Member> member = memberRepository.findMemberByEmailAndPassword(Email.emailFrom("polla@gmail.com"),
                Password.passwordFrom("pollari99"));

        assertTrue(member.isPresent());
    }
}
