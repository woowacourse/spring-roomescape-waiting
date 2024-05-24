package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static roomescape.model.Member.createMember;
import static roomescape.model.Reservation.createAcceptReservation;
import static roomescape.service.fixture.TestThemeFactory.createTheme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.model.Member;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

@DataJpaTest
@Sql("/init-data.sql")
class ThemeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("테마를 조회한다.")
    @Test
    void should_find_all_themes() {
        Theme theme1 = saveTheme(createTheme(1L));
        Theme theme2 = saveTheme(createTheme(2L));

        List<Theme> themes = themeRepository.findAll();

        assertThat(themes).extracting(Theme::getName).containsOnly(theme1.getName(), theme2.getName());
    }

    @DisplayName("테마를 조회한다.")
    @Test
    void should_save_theme() {
        Theme theme = saveTheme(createTheme(1L));

        List<Theme> themes = themeRepository.findAll();

        assertThat(themes).extracting(Theme::getName).containsOnly(theme.getName());
    }

    @DisplayName("아이디로 테마를 조회한다.")
    @Test
    void should_find_theme_when_give_theme_id() {
        Theme theme1 = saveTheme(createTheme(1L));
        Theme theme2 = saveTheme(createTheme(2L));

        Theme theme = themeRepository.findById(1L).get();

        assertThat(theme).extracting(Theme::getName).isEqualTo(theme1.getName());
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void should_delete_theme() {
        saveTheme(createTheme(1L));
        saveTheme(createTheme(2L));

        themeRepository.deleteById(1L);

        assertThat(themeRepository.count()).isEqualTo(1);
    }

    @DisplayName("특정 기간의 테마를 인기순으로 정렬하여 조회한다.")
    @Test
    void should_find_ranking_theme_by_date() {
        entityManager.persist(new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime reservationTime = entityManager.find(ReservationTime.class, 1L);
        entityManager.persist(createMember("무빈", "movin@email.com", "1234"));
        Member member = entityManager.find(Member.class, 1L);

        for (int i = 1; i <= 15; i++) {
            entityManager.persist(new Theme("name" + i, "description" + i, "thumbnail" + i));
        }

        for (int i = 1; i <= 10; i++) {
            Theme theme = entityManager.find(Theme.class, i);
            entityManager.persist(createAcceptReservation(LocalDate.now(), reservationTime, theme, member));
        }
        Theme theme10 = entityManager.find(Theme.class, 10);
        Theme theme9 = entityManager.find(Theme.class, 9);
        entityManager.persist(createAcceptReservation(LocalDate.now(), reservationTime, theme10, member));
        entityManager.persist(createAcceptReservation(LocalDate.now(), reservationTime, theme10, member));
        entityManager.persist(createAcceptReservation(LocalDate.now(), reservationTime, theme10, member));
        entityManager.persist(createAcceptReservation(LocalDate.now(), reservationTime, theme9, member));
        entityManager.persist(createAcceptReservation(LocalDate.now(), reservationTime, theme9, member));

        LocalDate before = LocalDate.now().minusDays(8);
        LocalDate after = LocalDate.now().plusDays(1);
        List<Theme> themes = themeRepository.findByDateBetweenOrderByTheme(before, after);

        assertSoftly(softly -> {
            softly.assertThat(themes).hasSize(10);
            softly.assertThat(themes).containsExactly(
                    new Theme(10L, "name10", "description10", "thumbnail10"),
                    new Theme(9L, "name9", "description9", "thumbnail9"),
                    new Theme(1L, "name1", "description1", "thumbnail1"),
                    new Theme(2L, "name2", "description2", "thumbnail2"),
                    new Theme(3L, "name3", "description3", "thumbnail3"),
                    new Theme(4L, "name4", "description4", "thumbnail4"),
                    new Theme(5L, "name5", "description5", "thumbnail5"),
                    new Theme(6L, "name6", "description6", "thumbnail6"),
                    new Theme(7L, "name7", "description7", "thumbnail7"),
                    new Theme(8L, "name8", "description8", "thumbnail8")
            );
        });
    }

    public Theme saveTheme(Theme theme) {
        entityManager.merge(theme);
        return theme;
    }
}
