package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)   // TODO : 더 좋은 데이터 초기화 방식 고민
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
        final Theme theme = Theme.of(
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
                () -> assertThat(savedTheme.getName().getName()).isEqualTo(theme.getName().getName()),
                () -> assertThat(savedTheme.getDescription().getDescription()).isEqualTo(theme.getDescription().getDescription()),
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

    @DisplayName("특정 기간 중 가장 예약 개수가 많은 상위 10개의 테마 정보를 인기순으로 조회한다.")
    @Test
    void findPopularThemes() {
        // When
        final ReservationDate startAt = new ReservationDate(LocalDate.now().minusDays(7));
        final ReservationDate endAt = new ReservationDate(LocalDate.now().minusDays(1));
        final int maximumThemeCount = 10;

        final List<Theme> popularThemes = themeRepository.findPopularThemes(startAt.getDate(), endAt.getDate(), maximumThemeCount);

        // Then
        assertAll(
                () -> assertThat(popularThemes.size()).isLessThanOrEqualTo(10),
                () -> assertThat(popularThemes.get(0).getId()).isEqualTo(1),
                () -> assertThat(popularThemes.get(1).getId()).isEqualTo(2),
                () -> assertThat(popularThemes.get(2).getId()).isEqualTo(10)
        );
    }
}
