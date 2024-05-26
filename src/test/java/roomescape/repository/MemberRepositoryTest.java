package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.MEMBER1_EMAIL;
import static roomescape.TestFixture.MEMBER1_PASSWORD;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.DBTest;
import roomescape.domain.Member;

class MemberRepositoryTest extends DBTest {

    @DisplayName("아이디와 비밀번호로 회원을 조회한다.")
    @Test
    void findByEmailAndPassword() {
        // given
        memberRepository.save(MEMBER1);

        // when
        Optional<Member> memberOptional = memberRepository.findByEmailAndPassword(MEMBER1_EMAIL, MEMBER1_PASSWORD);

        // then
        assertThat(memberOptional.isPresent()).isTrue();
        assertThat(memberOptional.get().getEmail()).isEqualTo(MEMBER1_EMAIL);
        assertThat(memberOptional.get().getPassword()).isEqualTo(MEMBER1_PASSWORD);
    }
}
