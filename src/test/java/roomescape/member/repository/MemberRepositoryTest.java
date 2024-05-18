package roomescape.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.util.JpaRepositoryTest;

@JpaRepositoryTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("동일한 이메일인 회원을 조회한다.")
    void findByEmail() {
        String email = "asdf1@asdf.com";
        Member member = memberRepository.save(MemberFixture.getOne(email));
        memberRepository.save(MemberFixture.getOne("dk" +email));

        assertThat(memberRepository.findByEmail(new Email(email))).isEqualTo(Optional.of(member));
    }

    @Test
    @DisplayName("동일한 이메일인 회원이 없는 경우, 빈 옵셔널을 반환한다.")
    void findByEmail_WhenNotExist() {
        String email = "asdf1@asdf.com";
        assertThat(memberRepository.findByEmail(new Email(email))).isEmpty();
    }
}
