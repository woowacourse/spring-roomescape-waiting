package roomescape.dao;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import static java.time.Month.FEBRUARY;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.theme.NotFoundThemeException;

@SpringBootTest
class ThemeDaoTest {
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private ReservationTimeDao reservationTimeDao;
    @Autowired
    private ReservationDao reservationDao;

    @AfterEach
    void tearDown() {
        reservationDao.deleteAll();
        themeDao.deleteAll();
        reservationTimeDao.deleteAll();
    }

    @Test
    @DisplayName("테마를 저장한다.")
    void save_ShouldSavePersistenceOfTheme() {
        // given
        Theme theme1 = new Theme("name1", "description", "thumbnail");

        // when
        themeDao.save(theme1);

        // then
        Assertions.assertThat(themeDao.findAll())
                .hasSize(1)
                .extracting("name", "description", "thumbnail")
                .containsExactlyInAnyOrder(
                        tuple("name1", "description", "thumbnail")
                );
    }

    @Test
    @DisplayName("기간 따른 테마 조회가능 하다")
    void findThemesByPeriodWithLimit_ShouldReturnThemes() {
        // given
        Member member = new Member("name", "email", "password");
        ReservationTime time1 = new ReservationTime(LocalTime.of(1, 0));
        Theme theme1 = new Theme("name1", "description", "thumbnail");
        Theme theme2 = new Theme("name2", "description", "thumbnail");
        Theme theme3 = new Theme("name3", "description", "thumbnail");
        Theme theme4 = new Theme("name4", "description", "thumbnail");

        ReservationTime savedTime = reservationTimeDao.save(time1);
        Theme savedTheme1 = themeDao.save(theme1);
        Theme savedTheme2 = themeDao.save(theme2);
        Theme savedTheme3 = themeDao.save(theme3);
        Theme savedTheme4 = themeDao.save(theme4);
        reservationDao.save(new Reservation(LocalDate.of(2023, FEBRUARY, 1), savedTime, savedTheme1, member));
        reservationDao.save(new Reservation(LocalDate.of(2023, FEBRUARY, 2), savedTime, savedTheme2, member));
        reservationDao.save(new Reservation(LocalDate.of(2023, FEBRUARY, 3), savedTime, savedTheme3, member));
        reservationDao.save(new Reservation(LocalDate.of(2023, FEBRUARY, 4), savedTime, savedTheme4, member));

        // when
        List<Theme> themesByPeriodWithLimit = themeDao.findThemesByPeriodWithLimit("2023-02-02", "2023-02-03", 5);

        // then

        Assertions.assertThat(themesByPeriodWithLimit)
                .containsExactlyInAnyOrder(
                        savedTheme2,
                        savedTheme3
                );
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다")
    void delete_ShouldRemovePersistence() {
        // given
        Theme theme = new Theme("name", "description", " thumbnail");
        Theme savedTheme = themeDao.save(theme);

        // when
        themeDao.delete(savedTheme);

        // then
        Assertions.assertThat(themeDao.findById(savedTheme.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("없는 테마를 삭제할 경우 예외를 던진다.")
    void delete_ShouldThrowException_WhenThemeDoesNotExists() {
        // given
        Theme theme = new Theme("name", "description", " thumbnail");

        // when & then
        Assertions.assertThatThrownBy(() -> themeDao.delete(theme))
                .isInstanceOf(NotFoundThemeException.class);
    }
}
