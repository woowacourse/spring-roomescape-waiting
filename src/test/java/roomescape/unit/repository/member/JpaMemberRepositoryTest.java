package roomescape.unit.repository.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.repository.member.JpaMemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class JpaMemberRepositoryTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    void username으로_멤버를_찾을_수_있다() {
        // Given
        String username = "user@user.com";
        Member member = jpaMemberRepository.save(new Member(null, username, "password", "user", Role.USER));

        // When & Then
        assertThat(jpaMemberRepository.findByUsername(username).get()).isEqualTo(member);
    }

    @Test
    void username으로_멤버가_존재하는지_확인할_수_있다() {
        // Given
        String username = "user@user.com";
        jpaMemberRepository.save(new Member(null, username, "password", "user", Role.USER));

        // When & Then
        assertAll(() -> {
            assertThat(jpaMemberRepository.existsByUsername(username)).isTrue();
            assertThat(jpaMemberRepository.existsByUsername("invalid")).isFalse();
        });
    }
}
