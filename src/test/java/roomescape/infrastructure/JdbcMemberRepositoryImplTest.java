package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Member;
import roomescape.domain.MemberEmail;
import roomescape.domain.MemberName;
import roomescape.domain.MemberPassword;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;

@JdbcTest
class JdbcMemberRepositoryImplTest {

    private final MemberRepository memberRepository;

    @Autowired
    JdbcMemberRepositoryImplTest(JdbcTemplate jdbcTemplate) {
        memberRepository = new JdbcMemberRepositoryImpl(jdbcTemplate);
    }

    @DisplayName("email, password로 해당되는 멤버 정보를 조회한다.")
    @Test
    void findByEmailAndPassword() {
        Member newMember = new Member(new MemberName("zay"), new MemberEmail("hihi@hello.com"),
            new MemberPassword("zzz123"), MemberRole.USER);

        Member expected = memberRepository.save(newMember);
        Member actual = memberRepository.findByEmailAndPassword("hihi@hello.com", "zzz123").get();
        assertThat(actual.getId()).isEqualTo(expected.getId());
    }

    @DisplayName("email, password를 가진 멤버가 없으면 빈 Optional을 반환한다.")
    @Test
    void findByEmailAndPassword_NotFoundMember() {
        Optional<Member> member = memberRepository.findByEmailAndPassword("hihi@hello.com", "zzz123");

        assertThat(member).isEmpty();
    }
}
