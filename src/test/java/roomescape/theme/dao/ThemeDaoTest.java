package roomescape.theme.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
@Import(ThemeDao.class)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ThemeDaoTest {

    @Autowired
    private ThemeDao themeDao;

    @Test
    @DisplayName("데이터베이스에서 모든 테마 정보를 조회한다.")
    void findAllThemes() {
        // given
        List<Theme> themes = themeDao.findAll();

        // when & then
        assertThat(themes.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("데이터베이스에 테마 정보를 추가한다.")
    void saveTheme() {
        // given
        Theme theme = themeDao.insert(new Theme("name", "description", "thumbnail"));

        // when & then
        assertThat(theme.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("테마 id를 통해 데이터베이스에서 테마를 삭제한다.")
    void deleteTheme() {
        // given
        Theme theme = themeDao.insert(new Theme("name", "description", "thumbnail"));

        // when
        int deleteCount = themeDao.deleteById(theme.getId());

        // then
        assertThat(deleteCount).isEqualTo(1);
    }

    /*
     *  reservationData DataSet ThemeID 별 reservation 개수
     *  5,4,2,5,2,3,1,1,1,1,1
     *  예약 수 내림차순 + ThemeId 오름차순 정렬 순서
     *  1, 4, 2, 6, 3, 5, 7, 8, 9, 10
     */
    @Test
    @DisplayName("예약 수 상위 10개 테마를 조회했을 때 내림차순으로 정렬된다. 만약 예약 수가 같다면, id 순으로 오름차순 정렬된다.")
    @Sql(scripts = {"/truncate.sql", "/reservationData.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    void readTop10ThemesDescOrder() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(1);

        // when
        List<Theme> top10Reservations = themeDao.findByStartDateAndEndDateWithLimit(startDate, endDate, 10);

        // then
        assertAll(
                () -> assertThat(top10Reservations.size()).isEqualTo(10),
                () -> assertThat(top10Reservations.get(0).getId()).isEqualTo(1),
                () -> assertThat(top10Reservations.get(1).getId()).isEqualTo(4),
                () -> assertThat(top10Reservations.get(2).getId()).isEqualTo(2),
                () -> assertThat(top10Reservations.get(3).getId()).isEqualTo(6),
                () -> assertThat(top10Reservations.get(4).getId()).isEqualTo(3),
                () -> assertThat(top10Reservations.get(5).getId()).isEqualTo(5),
                () -> assertThat(top10Reservations.get(6).getId()).isEqualTo(7),
                () -> assertThat(top10Reservations.get(7).getId()).isEqualTo(8),
                () -> assertThat(top10Reservations.get(8).getId()).isEqualTo(9),
                () -> assertThat(top10Reservations.get(9).getId()).isEqualTo(10)
        );
    }
}
