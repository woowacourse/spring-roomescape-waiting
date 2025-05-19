package roomescape.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles("test")
@DataJpaTest
class JpaThemeRepositoryTest {

    @Autowired
    private JpaThemeRepository repository;

    @PersistenceContext
    private EntityManager em;


    @Test
    @DisplayName("인기 테마를 개수에 맞게 조회할 수 있다.")
    void findRankByDateByLimit() {
        // given
        List<Theme> themes = TestFixture.setupThemeRankTestCaseByLimit(em);

        // when
        int limit = 2;
        List<Theme> result = repository.findRankByDate(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 7), limit);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.getFirst()).isEqualTo(themes.getFirst())
        );
    }

    @Test
    @DisplayName("인기 테마를 날짜에 맞게 조회할 수 있다.")
    void findRankByDateByDate() {
        // given
        List<Theme> themes = TestFixture.setupThemeRankTestCaseByDateRange(em);

        // when
        int limit = 2;
        List<Theme> result = repository.findRankByDate(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), limit);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.getFirst()).isEqualTo(themes.getFirst())
        );
    }
}