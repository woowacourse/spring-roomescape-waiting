package roomescape.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.model.Role.MEMBER;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql(scripts = "/test_data.sql")
class ThemeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("테마를 조회한다.")
    @Test
    void should_find_all_themes() {
        entityManager.persist(new Theme("무빈", "공포", "공포.jpg"));
        entityManager.persist(new Theme("배키", "미스터리", "미스터리.jpg"));

        List<Theme> themes = themeRepository.findAll();

        assertThat(themes).extracting(Theme::getName).containsOnly("무빈", "배키");
    }

    @DisplayName("테마를 조회한다.")
    @Test
    void should_save_theme() {
        themeRepository.save(new Theme("무빈", "공포", "공포.jpg"));

        List<Theme> themes = themeRepository.findAll();

        assertThat(themes).extracting(Theme::getName).containsOnly("무빈");
    }

    @DisplayName("아이디로 테마를 조회한다.")
    @Test
    void should_find_theme_when_give_theme_id() {
        entityManager.persist(new Theme("무빈", "공포", "공포.jpg"));
        entityManager.persist(new Theme("배키", "미스터리", "미스터리.jpg"));

        Theme theme = themeRepository.findById(1L).get();

        assertThat(theme).extracting(Theme::getName).isEqualTo("무빈");
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void should_delete_theme() {
        entityManager.persist(new Theme("무빈", "공포", "공포.jpg"));
        entityManager.persist(new Theme("배키", "미스터리", "미스터리.jpg"));

        themeRepository.deleteById(1L);

        assertThat(themeRepository.count()).isEqualTo(1);
    }

    @DisplayName("특정 기간의 테마를 인기순으로 정렬하여 조회한다.")
    @Test
    void should_find_ranking_theme_by_date() {
        entityManager.persist(new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime reservationTime = entityManager.find(ReservationTime.class, 1L);
        entityManager.persist(new Member("무빈", MEMBER, "email@email", "1234"));
        Member member = entityManager.find(Member.class, 1L);

        for (int i = 1; i <= 15; i++) {
            entityManager.persist(new Theme("name" + i, "description" + i, "thumbnail" + i));
        }

        for (int i = 1; i <= 10; i++) {
            Theme theme = entityManager.find(Theme.class, i);
            entityManager.persist(new Reservation(LocalDate.now(), reservationTime, theme, member));
        }
        Theme theme10 = entityManager.find(Theme.class, 10);
        Theme theme9 = entityManager.find(Theme.class, 9);
        entityManager.persist(new Reservation(LocalDate.now(), reservationTime, theme10, member));
        entityManager.persist(new Reservation(LocalDate.now(), reservationTime, theme10, member));
        entityManager.persist(new Reservation(LocalDate.now(), reservationTime, theme10, member));
        entityManager.persist(new Reservation(LocalDate.now(), reservationTime, theme9, member));
        entityManager.persist(new Reservation(LocalDate.now(), reservationTime, theme9, member));

        LocalDate before = LocalDate.now().minusDays(8);
        LocalDate after = LocalDate.now().plusDays(1);
        List<Theme> themes = themeRepository.findFirst10ByDateBetweenOrderByTheme(before, after);

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
}
