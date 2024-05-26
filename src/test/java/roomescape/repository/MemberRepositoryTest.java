package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DBTest;
import roomescape.TestFixture;
import roomescape.domain.Member;

class MemberRepositoryTest extends DBTest {

    @DisplayName("아이디와 비밀번호로 회원을 조회한다.")
    @Test
    void findByEmailAndPassword() {
        // given
        memberRepository.save(TestFixture.getMember1());

        // when
        Optional<Member> memberOptional = memberRepository.findByEmailAndPassword(TestFixture.MEMBER1_EMAIL,
                TestFixture.MEMBER1_PASSWORD);

        // then
        assertThat(memberOptional.isPresent()).isTrue();
        assertThat(memberOptional.get().getEmail()).isEqualTo(TestFixture.MEMBER1_EMAIL);
        assertThat(memberOptional.get().getPassword()).isEqualTo(TestFixture.MEMBER1_PASSWORD);
    }
}
