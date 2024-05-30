package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.reservation.repository.fixture.ThemeFixture.THEME1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.fixture.ThemeFixture;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void findAll() {
        final var result = themeRepository.findAll();

        assertThat(result).hasSize(ThemeFixture.count());
    }

    @DisplayName("id로 테마를 조회한다.")
    @Test
    void findById() {
        final var result = themeRepository.findById(1L);

        assertThat(result.get()).isEqualTo(THEME1.create());
    }

    @DisplayName("테마를 생성한다.")
    @Test
    void save() {
        final var theme = new Theme("테마 이름", "테마 설명", "테마 썸네일");

        themeRepository.save(theme);

        assertThat(themeRepository.findAll()).hasSize(ThemeFixture.count() + 1);
    }

    @DisplayName("id로 테마를 삭제한다.")
    @Test
    void deleteById() {
        themeRepository.deleteById(4L);

        assertThat(themeRepository.findById(4L)).isEmpty();
    }
}
