package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import roomescape.theme.domain.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("테마 정보가 DB에 정상적으로 저장되는지 확인한다.")
    void saveTheme() {
        Theme theme = new Theme("포레스트", "공포 테마", "thumbnail");
        Theme actual = themeRepository.save(theme);

        List<Theme> expected = themeRepository.findAll();
        assertThat(actual).isEqualTo(expected.iterator()
                .next());
    }

    @Test
    @DisplayName("테마 정보들을 정상적으로 가져오는지 확인한다.")
    void getThemes() {
        entityManager.merge(new Theme("테마1", "설명1", "image.png"));
        entityManager.merge(new Theme("테마2", "설명2", "image.png"));
        entityManager.merge(new Theme("테마3", "설명3", "image.png"));

        List<Theme> themes = themeRepository.findAll();
        assertAll(() -> {
            assertThat(themes).hasSize(3);
            assertThat(themes).extracting(Theme::getName)
                    .containsOnly("테마1", "테마2", "테마3");
        });
    }

    @Test
    @DisplayName("테마 정보들이 정상적으로 제거되었는지 확인한다.")
    void deleteThemes() {
        entityManager.merge(new Theme("테마1", "설명1", "image.png"));
        entityManager.merge(new Theme("테마2", "설명2", "image.png"));
        entityManager.merge(new Theme("테마3", "설명3", "image.png"));

        themeRepository.deleteById(3L);

        assertThat(themeRepository.findAll()).hasSize(2);
    }

//    @Test
//    @DisplayName("지난 7일 기준 예약이 많은 테마 순으로 조회한다.")
//    void getTopReservationThemes() {
//        // Given
//        LocalDate reservationStartDate = LocalDate.now()
//                .minusDays(6);
//        Time time = new Time(LocalTime.of(12, 0));
//        Theme theme = new Theme("테마1", "설명1", "image.png");
//
//        //When
//        entityManager.merge(theme);
//        entityManager.merge(time);
//        entityManager.merge(new Reservation("도비", reservationStartDate, time, theme));
//
//        // Then
//        List<Theme> themes = themeRepository.findThemesByReservationDateOrderByReservationCountDesc(
//                reservationStartDate, reservationStartDate.plusWeeks(1));
//        Assertions.assertThat(themes)
//                .isEqualTo(new Theme(1L,"테마1", "설명1", "image.png"));
//    }

}
