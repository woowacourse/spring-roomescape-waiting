package roomescape.unit.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.infrastructure.ThemeRepository;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 테마이름으로_테마를_조회한다() {
        // given
        entityManager.persist(Theme.builder()
                .name("theme1")
                .description("desc")
                .thumbnail("thumb").build()
        );
        // when
        Optional<Theme> theme = themeRepository.findByName("theme1");
        // then
        assertThat(theme.isPresent()).isTrue();
        assertThat(theme.get().getName()).isEqualTo("theme1");
    }
}