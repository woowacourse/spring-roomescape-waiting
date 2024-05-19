package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.support.fixture.MemberFixture;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 회원을 조회한다.")
    void findByEmail() {
        Member member = MemberFixture.email("example@gmail.com");
        memberRepository.save(member);

        Optional<Member> memberOptional = memberRepository.findByEmail("example@gmail.com");

        assertThat(memberOptional).isPresent();
        assertThat(memberOptional.get().getEmail()).isEqualTo("example@gmail.com");
    }

    @Test
    @DisplayName("이메일에 해당하는 회원이 존재하는지 확인한다.")
    void existsByEmail() {
        Member member = MemberFixture.email("example@gmail.com");
        memberRepository.save(member);

        assertThat(memberRepository.existsByEmail("example@gmail.com")).isTrue();
        assertThat(memberRepository.existsByEmail("nothing@gmail.com")).isFalse();
    }
}
