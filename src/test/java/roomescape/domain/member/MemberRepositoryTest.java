package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.BaseRepositoryTest;
import roomescape.support.fixture.MemberFixture;

class MemberRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 회원을 조회한다.")
    void findByEmail() {
        String rawEmail = "example@gmail.com";
        save(MemberFixture.create(rawEmail));

        Email email = new Email(rawEmail);
        Member member = memberRepository.findByEmail(email).orElseThrow();

        assertThat(member.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("이메일에 해당하는 회원이 존재하면 true를 반환한다.")
    void existsByValidEmail() {
        save(MemberFixture.create("example@gmail.com"));

        assertThat(memberRepository.existsByEmail(new Email("example@gmail.com"))).isTrue();
    }

    @Test
    @DisplayName("이메일에 해당하는 회원이 존재하지 않으면 false를 반환한다.")
    void existsByInvalidEmail() {
        save(MemberFixture.create("example@gmail.com"));

        assertThat(memberRepository.existsByEmail(new Email("nothing@gmail.com"))).isFalse();
    }
}
