package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@JdbcTest
@Import({ThemeDao.class, ReservationDao.class, ReservationTimeDao.class})
@Sql("/truncate.sql")
class ThemeDaoTest {

    private static final LocalDate START = LocalDate.of(2026, 4, 28);
    private static final LocalDate END = LocalDate.of(2026, 5, 4);

    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationTimeDao timeDao;

    @Test
    @DisplayName("예약 수가 많은 순으로 테마를 반환한다.")
    void findPopularBetween_orderedByCount() {
        ReservationTime time = timeDao.save(ReservationTime.create(0, LocalTime.of(10, 0)));
        Theme popular = themeDao.save(Theme.create(0, "인기테마", "url", "설명"));
        Theme unpopular = themeDao.save(Theme.create(0, "비인기테마", "url", "설명"));

        reservationDao.save(Reservation.forNew(new Member("user_a"), new Slot(START, time, popular)));
        reservationDao.save(Reservation.forNew(new Member("user_b"), new Slot(START.plusDays(1), time, popular)));
        reservationDao.save(Reservation.forNew(new Member("user_c"), new Slot(START, time, unpopular)));

        List<Theme> result = themeDao.findPopularBetween(START, END, 10);

        assertThat(result).extracting(Theme::id)
                .containsExactly(popular.id(), unpopular.id());
    }

    @Test
    @DisplayName("기간 밖의 예약은 집계에서 제외된다.")
    void findPopularBetween_excludesOutOfRange() {
        ReservationTime time = timeDao.save(ReservationTime.create(0, LocalTime.of(10, 0)));
        Theme theme = themeDao.save(Theme.create(0, "테마", "url", "설명"));

        reservationDao.save(Reservation.forNew(new Member("user_a"), new Slot(END.plusDays(1), time, theme)));

        List<Theme> result = themeDao.findPopularBetween(START, END, 10);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("limit 수만큼만 반환한다.")
    void findPopularBetween_respectsLimit() {
        ReservationTime time = timeDao.save(ReservationTime.create(0, LocalTime.of(10, 0)));
        Theme theme1 = themeDao.save(Theme.create(0, "테마1", "url", "설명"));
        Theme theme2 = themeDao.save(Theme.create(0, "테마2", "url", "설명"));
        Theme theme3 = themeDao.save(Theme.create(0, "테마3", "url", "설명"));

        reservationDao.save(Reservation.forNew(new Member("user_a"), new Slot(START, time, theme1)));
        reservationDao.save(Reservation.forNew(new Member("user_b"), new Slot(START.plusDays(1), time, theme2)));
        reservationDao.save(Reservation.forNew(new Member("user_c"), new Slot(START.plusDays(2), time, theme3)));

        List<Theme> result = themeDao.findPopularBetween(START, END, 2);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("id에 해당하는 테마가 존재하면 true를 반환한다.")
    void existsById_existingTheme() {
        Theme theme = themeDao.save(Theme.create(0, "테마", "url", "설명"));

        assertThat(themeDao.existsById(theme.id())).isTrue();
    }

    @Test
    @DisplayName("id에 해당하는 테마가 없으면 false를 반환한다.")
    void existsById_unknownTheme() {
        assertThat(themeDao.existsById(999L)).isFalse();
    }
}
