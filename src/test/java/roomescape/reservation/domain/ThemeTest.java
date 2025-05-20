package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatNoException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class ThemeTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("테이블 생성 테스트")
    void createThemeTest() {
        assertThatNoException()
                .isThrownBy(() -> entityManager.persist(new Theme("theme1", "description", "thumbnail")));
    }
}
