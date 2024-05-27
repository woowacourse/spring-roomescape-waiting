package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.model.Theme;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("특정 아이디의 테마 정보를 조회한다.")
    @Test
    void findByIdTest() {
        // When
        final Long themeId = 1L;
        final Optional<Theme> theme = themeRepository.findById(themeId);

        // Then
        assertThat(theme.isPresent()).isTrue();
    }

    @DisplayName("모든 테마 정보를 조회한다.")
    @Test
    void find() {
        // When
        final List<Theme> themes = themeRepository.findAll();

        // Then
        assertThat(themes).hasSize(15);
    }

    @DisplayName("테마 정보를 저장한다.")
    @Test
    void saveTest() {
        // Given
        final Theme theme = new Theme(
                "테바의 비밀친구",
                "테바의 은밀한 비밀친구",
                "대충 테바 사진 링크");

        // When
        final Theme savedTheme = themeRepository.save(theme);

        // Then
        final List<Theme> themes = themeRepository.findAll();
        assertAll(
                () -> assertThat(themes).hasSize(16),
                () -> assertThat(savedTheme.getId()).isEqualTo(16L),
                () -> assertThat(savedTheme.getName().getValue()).isEqualTo(theme.getName().getValue()),
                () -> assertThat(savedTheme.getDescription().getValue()).isEqualTo(theme.getDescription().getValue()),
                () -> assertThat(savedTheme.getThumbnail()).isEqualTo(theme.getThumbnail())
        );
    }

    @DisplayName("테마 정보를 삭제한다.")
    @Test
    void deleteByIdTest() {
        // When
        themeRepository.deleteById(3L);

        // Then
        final long count = themeRepository.count();
        assertThat(count).isEqualTo(14);
    }
}
