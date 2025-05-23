package roomescape.member.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaMemberRepositoryTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    void 멤버를_저장하고_id로_조회할_수_있다() {
        Optional<Member> found = jpaMemberRepository.findById(1L);

        assertThat(found.get().getName()).isEqualTo("유저1");
    }

    @Test
    void 이메일로_멤버를_조회할_수_있다() {
        Optional<Member> found = jpaMemberRepository.findByEmail(new Email("member1@email.com"));

        assertThat(found.get().getId()).isEqualTo(1L);
    }

    @Test
    void 이메일_존재_여부를_확인할_수_있다() {
        boolean exists = jpaMemberRepository.existsByEmail(new Email("member1@email.com"));

        assertThat(exists).isTrue();
    }

    @Test
    void 모든_멤버를_조회할_수_있다() {
        List<Member> members = jpaMemberRepository.findAll();

        assertThat(members)
            .extracting(Member::getId)
            .containsExactly(1L, 2L, 3L);
    }

}