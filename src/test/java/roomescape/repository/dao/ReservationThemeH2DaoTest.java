package roomescape.repository.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.infrastructure.db.dao.ReservationThemeH2Dao;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.support.JdbcTestSupport;

@Import(ReservationThemeH2Dao.class)
class ReservationThemeH2DaoTest extends JdbcTestSupport {

    @Autowired
    private ReservationThemeH2Dao reservationThemeH2Dao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("저장한 모든 테마 조회할 수 있다.")
    @Test
    void selectAll() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름1", "설명1", "썸네일1");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름2", "설명2", "썸네일2");

        // when
        List<ReservationTheme> reservationThemes = reservationThemeH2Dao.selectAll();

        // then
        assertThat(reservationThemes).hasSize(2);
    }

    @DisplayName("ReservationTheme객체로 데이터를 삽입할 수 있다.")
    @Test
    void insert() {
        // given
        String name = "이름";
        String description = "설명";
        String thumbnail = "썸네일";
        ReservationTheme reservationTheme = new ReservationTheme(name, description, thumbnail);

        // when
        reservationThemeH2Dao.insertAndGet(reservationTheme);

        // then
        List<ReservationTheme> reservationThemes = reservationThemeH2Dao.selectAll();

        assertSoftly(softly -> {
            softly.assertThat(reservationThemes).hasSize(1);
            softly.assertThat(reservationThemes.getFirst().getName()).isEqualTo(name);
            softly.assertThat(reservationThemes.getFirst().getDescription()).isEqualTo(description);
            softly.assertThat(reservationThemes.getFirst().getThumbnail()).isEqualTo(thumbnail);
        });
    }

    @DisplayName("ReservationTheme객체로 데이터를 삽입 후 삽입된 데이터를 가져올 수 있다.")
    @Test
    void insertAndGet() {
        // given
        String name = "이름";
        String description = "설명";
        String thumbnail = "썸네일";
        ReservationTheme reservationTheme = new ReservationTheme(name, description, thumbnail);

        // when
        ReservationTheme insertedReservationTheme = reservationThemeH2Dao.insertAndGet(reservationTheme);

        // then
        assertSoftly(softly -> {
            softly.assertThat(insertedReservationTheme.getName()).isEqualTo(name);
            softly.assertThat(insertedReservationTheme.getDescription()).isEqualTo(description);
            softly.assertThat(insertedReservationTheme.getThumbnail()).isEqualTo(thumbnail);
        });
    }

    @DisplayName("id에 해당하는 테마가 없다면 빈 값을 반환한다.")
    @Test
    void noneSelect() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름1", "설명1", "썸네일1");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름2", "설명2", "썸네일2");

        // when
        assertThatThrownBy(() -> reservationThemeH2Dao.selectById(3L).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("id에 해당하는 테마를 조회할 수 있다.")
    @Test
    void selectByGetGetId() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름1", "설명1", "썸네일1");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름2", "설명2", "썸네일2");

        // when
        Optional<ReservationTheme> selectedReservationTheme = reservationThemeH2Dao.selectById(2L);

        // then
        assertThatCode(selectedReservationTheme::get).doesNotThrowAnyException();
        ReservationTheme reservationTheme = selectedReservationTheme.get();

        assertSoftly(softly -> {
            softly.assertThat(reservationTheme.getName()).isEqualTo("이름2");
            softly.assertThat(reservationTheme.getDescription()).isEqualTo("설명2");
            softly.assertThat(reservationTheme.getThumbnail()).isEqualTo("썸네일2");
        });
    }

    @DisplayName("id에 해당하는 테마을 삭제할 수 있다.")
    @Test
    void deleteByGetGetId() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름1", "설명1", "썸네일1");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름2", "설명2", "썸네일2");

        // when
        reservationThemeH2Dao.deleteById(2L);

        // then
        assertThatThrownBy(() -> reservationThemeH2Dao.selectById(2L).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("테마의 인기 목록을 입력한 개수만큼 조회할 수 있다")
    @Test
    void orderByGetThemeBookedCountWithLimit() {
        // given
        int limit = 2;
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름1", "설명1", "썸네일1");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름2", "설명2", "썸네일2");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름3", "설명3", "썸네일3");

        jdbcTemplate.update("INSERT INTO RESERVATION(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                LocalDate.now().plusDays(20), 1L, 1L);
        jdbcTemplate.update("INSERT INTO RESERVATION(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                LocalDate.now().plusDays(20), 1L, 2L);
        jdbcTemplate.update("INSERT INTO RESERVATION(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                LocalDate.now().plusDays(21), 1L, 2L);

        // when
        List<ReservationTheme> populars = reservationThemeH2Dao.getOrderByThemeBookedCountWithLimit(
                limit);

        // then
        assertSoftly(softly -> {
            softly.assertThat(populars).hasSize(limit);
            softly.assertThat(populars.getFirst().getId()).isEqualTo(2L);
            softly.assertThat(populars.get(1).getId()).isEqualTo(1L);
        });
    }
}
