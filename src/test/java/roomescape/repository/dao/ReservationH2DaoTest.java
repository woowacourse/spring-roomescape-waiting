package roomescape.repository.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.infrastructure.db.dao.ReservationH2Dao;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.support.JdbcTestSupport;


@Import(ReservationH2Dao.class)
class ReservationH2DaoTest extends JdbcTestSupport {

    @Autowired
    private ReservationH2Dao reservationH2Dao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름", "설명", "썸네일");
        jdbcTemplate.update("INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)",
                "브라운", "admin@naver.com", "1234", "ADMIN");
        jdbcTemplate.update("INSERT INTO member (name, email, password, role) VALUES (?, ?, ?, ?)",
                "웨이드", "user@naver.com", "1234", "USER");
    }

    @DisplayName("저장한 모든 예약을 조회할 수 있다.")
    @Test
    void selectAll() {
        // given
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운",
                LocalDate.now().plusDays(20), "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운",
                LocalDate.now().plusDays(21), "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "브라운",
                LocalDate.now().plusDays(22), "1", "1");

        // when
        List<Reservation> reservations = reservationH2Dao.selectAll();

        // then
        assertThat(reservations).hasSize(3);
    }

    @DisplayName("Reservation객체로 데이터를 삽입할 수 있다.")
    @Test
    void insert() {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 20));
        ReservationTheme reservationTheme = new ReservationTheme(1L, "테마 이름", "설명", "썸네일");
        Reservation reservation = new Reservation("브라운", LocalDate.now().plusDays(20), reservationTime,
                reservationTheme);

        // when
        reservationH2Dao.insertAndGet(reservation);

        // then
        List<Reservation> reservations = reservationH2Dao.selectAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getName()).isEqualTo("브라운");
    }

    @DisplayName("Reservation객체로 데이터를 삽입 후 삽입된 데이터를 가져올 수 있다.")
    @Test
    void insertAndGet() {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 20));
        ReservationTheme reservationTheme = new ReservationTheme(1L, "테마 이름", "설명", "썸네일");
        Reservation reservation = new Reservation("브라운", LocalDate.now().plusDays(20), reservationTime,
                reservationTheme);

        // when
        Reservation insertedReservation = reservationH2Dao.insertAndGet(reservation);

        // then
        assertSoftly(softly -> {
            softly.assertThat(insertedReservation.getTime().getId()).isEqualTo(reservationTime.getId());
            softly.assertThat(insertedReservation.getTheme().getId()).isEqualTo(reservationTheme.getId());
            softly.assertThat(insertedReservation.getName()).isEqualTo("브라운");
        });
    }

    @DisplayName("id에 해당하는 예약이 없다면 빈 값을 반환한다.")
    @Test
    void noneSelect() {
        // given
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                LocalDate.now().plusDays(20), "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "2번사람",
                LocalDate.now().plusDays(21), "1", "1");
        // when
        assertThatThrownBy(() -> reservationH2Dao.selectById(3L).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("id에 해당하는 예약 정보를 조회할 수 있다.")
    @Test
    void selectByGetId() {
        // given
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                LocalDate.now().plusDays(20), "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "2번사람",
                LocalDate.now().plusDays(21), "1", "1");

        // when
        Optional<Reservation> selectedReservation = reservationH2Dao.selectById(2L);

        // then
        assertThatCode(selectedReservation::get).doesNotThrowAnyException();
        Reservation reservation = selectedReservation.get();

        assertThat(reservation.getName()).isEqualTo("2번사람");
    }

    @DisplayName("id에 해당하는 예약 정보를 삭제할 수 있다.")
    @Test
    void deleteByGetId() {
        // given
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                LocalDate.now().plusDays(20), "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "2번사람",
                LocalDate.now().plusDays(21), "1", "1");

        // when
        reservationH2Dao.deleteById(2L);

        // then
        assertThatThrownBy(() -> reservationH2Dao.selectById(2L).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("입력한 date, timeId, themeId의 값을 가진 데이터가 존재하면, true를 반환한다")
    @Test
    void existDuplicatedGetDateGetTimeReturnTrue() {
        // given
        LocalDate date = LocalDate.now().plusDays(20);
        Long timeId = 1L;
        Long themeId = 1L;
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, timeId, themeId);

        // when
        boolean isExistDuplicatedDateTime = reservationH2Dao.existDuplicatedDateTime(date, timeId, themeId);

        // then
        assertThat(isExistDuplicatedDateTime).isTrue();
    }

    @DisplayName("입력한 date, timeId, themeId의 값을 가진 데이터가 존재하지 않면, false를 반환한다")
    @Test
    void existDuplicatedGetDateGetTimeReturnFalse() {
        // given
        LocalDate date = LocalDate.now().plusDays(20);
        Long timeId = 1L;
        Long themeId = 1L;
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, timeId, themeId);

        Long anotherId = 100L;
        // when
        boolean isExistDuplicatedDateTime = reservationH2Dao.existDuplicatedDateTime(date, anotherId, themeId);

        // then
        assertThat(isExistDuplicatedDateTime).isFalse();
    }

    @DisplayName("입력한 timeId를 가진 예약이 존재하면 true를 반환한다")
    @Test
    void existsByGetTimeGetId() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        Long timeId = 1L;
        LocalDate date = LocalDate.now().plusDays(20);
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, timeId, 1L);

        // when
        boolean existsByTimeId = reservationH2Dao.existsByTimeId(timeId);

        // then
        assertThat(existsByTimeId).isTrue();
    }

    @DisplayName("입력한 timeId를 가진 예약이 존재하지 않으면 false를 반환한다")
    @Test
    void notExistsByGetTimeGetId() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        Long timeId = 1L;
        LocalDate date = LocalDate.now().plusDays(20);
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, timeId, 1L);

        Long anotherTimeId = 100L;
        // when
        boolean existsByTimeId = reservationH2Dao.existsByTimeId(anotherTimeId);

        // then
        assertThat(existsByTimeId).isFalse();
    }

    @DisplayName("입력한 themeId를 가진 예약이 존재하면 true를 반환한다")
    @Test
    void existsByGetThemeGetId() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(20);
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, 1L, themeId);

        // when
        boolean existsByThemeId = reservationH2Dao.existsByThemeId(themeId);

        // then
        assertThat(existsByThemeId).isTrue();
    }

    @DisplayName("입력한 themeId를 가진 예약이 존재하지 않으면 false를 반환한다")
    @Test
    void notExistsByGetThemeGetId() {
        // given
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름", "설명", "썸네일");
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(20);
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, 1L, themeId);

        Long anotherThemeId = 100L;

        // when
        boolean existsByThemeId = reservationH2Dao.existsByThemeId(anotherThemeId);

        // then
        assertThat(existsByThemeId).isFalse();
    }

    @DisplayName("조건이 없다면 전체 조회한다.")
    @Test
    void selectByNonFilter() {
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "웨이드", LocalDate.now().plusDays(6), "1", "1", "2");
        List<Reservation> result = reservationH2Dao.selectByFilter(null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("브라운");
        assertThat(result.get(1).getName()).isEqualTo("웨이드");
    }

    @DisplayName("멤버 ID로 조회할 수 있다.")
    @Test
    void selectByFilterMemberId() {

        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "웨이드", LocalDate.now().plusDays(6), "1", "1", "2");
        List<Reservation> result = reservationH2Dao.selectByFilter(null, 1L, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("브라운");
    }

    @DisplayName("테마 ID로 조회할 수 있다.")
    @Test
    void selectByFilterThemeId() {
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "웨이드", LocalDate.now().plusDays(6), "1", "1", "2");
        List<Reservation> result = reservationH2Dao.selectByFilter(1L, null, null, null);

        assertThat(result).hasSize(2);
    }

    @DisplayName("기간 필터링으로 조회 가능하다.")
    @Test
    void selectByFilterDate() {
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "웨이드", LocalDate.now().plusDays(6), "1", "1", "2");
        List<Reservation> result = reservationH2Dao.selectByFilter(null, null,
                LocalDate.now().plusDays(6), LocalDate.now().plusDays(6));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("웨이드");
    }

    @DisplayName("조건에 맞는 값이 없다면 빈값을 반환한다.")
    @Test
    void selectByFilterEmpty() {
        jdbcTemplate.update("INSERT INTO reservation(name, date, time_id, theme_id, member_id) VALUES (?, ?, ?, ?, ?)",
                "브라운", LocalDate.now().plusDays(5), "1", "1", "1");
        List<Reservation> result = reservationH2Dao.selectByFilter(null, 9999L, null, null);

        assertThat(result).isEmpty();
    }
}
