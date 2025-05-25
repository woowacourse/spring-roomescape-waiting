package roomescape.domain.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Role;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 회원을 조회할 수 있다")
    void findByEmail_shouldReturnMember() {
        // given
        Member member = Member.withoutId("유저", "user@example.com", "1234", Role.USER);
        memberRepository.save(member);

        // when
        Optional<Member> foundMember = memberRepository.findByEmail("user@example.com");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getName()).isEqualTo("유저");
        assertThat(foundMember.get().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다")
    void findByEmail_shouldReturnEmptyIfNotExists() {
        // when
        Optional<Member> result = memberRepository.findByEmail("user@example.com");

        // then
        assertThat(result).isNotPresent();
    }
}