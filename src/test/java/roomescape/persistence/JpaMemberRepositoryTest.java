package roomescape.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.Member;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class JpaMemberRepositoryTest {

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    void findByEmail() {
        //given
        jpaMemberRepository.save(TestFixture.createDefaultMember());

        //when
        Optional<Member> result = jpaMemberRepository.findByEmail("email");

        //then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getEmail()).isEqualTo("email");
    }
}