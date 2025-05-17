package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Theme;

@DataJpaTest
class JpaThemeRepositoryTest {

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 테마이름으로_테마를_조회한다() {
        // given
        entityManager.persist(Theme.createWithoutId("theme1", "desc", "thumb"));
        // when
        Optional<Theme> theme = jpaThemeRepository.findByName("theme1");
        // then
        assertThat(theme.isPresent()).isTrue();
        assertThat(theme.get().getName()).isEqualTo("theme1");
    }
}