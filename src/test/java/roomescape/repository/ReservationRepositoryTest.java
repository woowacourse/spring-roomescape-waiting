package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
class ReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ReservationRepository reservationRepository;

    private LocalDate date = LocalDate.of(2023, 8, 5);

    @BeforeEach
    void setup() {
        this.reservationRepository = new ReservationRepository(jdbcTemplate);
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
    }

    @Test
    void 예약_추가_테스트() {
        // given
        ReservationTime time = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        Reservation reservation = new Reservation(null, "브라운", date, time, theme);

        // when
        Long id = reservationRepository.insert(reservation);

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        Reservation savedReservation = reservationRepository.findById(id).get();
        assertAll(
                () -> assertThat(id).isNotNull(),
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(savedReservation.getName()).isEqualTo(reservation.getName()),
                () -> assertThat(savedReservation.getDate()).isEqualTo(reservation.getDate()),
                () -> assertThat(savedReservation.getTime().getStartAt()).isEqualTo(reservation.getTime().getStartAt()));
    }

    @Test
    void 예약_삭제_테스트() {
        // given
        ReservationTime time1 = findTimeByStartAt("15:00");
        Theme theme1 = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        ReservationTime time2 = findTimeByStartAt("12:00");
        Theme theme2 = new Theme(2L, "테마 이름2", "테마 설명2", "썸네일2");
        Reservation reservation1 = new Reservation(null, "브라운", date, time1, theme1);
        Reservation reservation2 = new Reservation(null, "구구", date, time2, theme2);
        Long id1 = reservationRepository.insert(reservation1);
        Long id2 = reservationRepository.insert(reservation2);

        // when
        int deletedCount = reservationRepository.delete(id1);

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertAll(
                () -> assertThat(deletedCount).isEqualTo(1),
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservationRepository.findById(id1)).isEmpty());
    }

    @Test
    void 이름에_해당하는_예약_목록을_조회한다() {
        // given
        ReservationTime time1 = findTimeByStartAt("15:00");
        ReservationTime time2 = findTimeByStartAt("12:00");
        Theme theme1 = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        Theme theme2 = new Theme(2L, "테마 이름2", "테마 설명2", "썸네일2");
        reservationRepository.insert(new Reservation(null, "브라운", date, time1, theme1));
        reservationRepository.insert(new Reservation(null, "브라운", date.plusDays(1), time2, theme2));
        reservationRepository.insert(new Reservation(null, "구구", date, time2, theme2));

        // when
        List<Reservation> result = reservationRepository.findByName("브라운");

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Reservation::getName)
                        .containsExactly("브라운", "브라운"),
                () -> assertThat(result).extracting(Reservation::getDate)
                        .containsExactly(date, date.plusDays(1)));
    }

    @Test
    void 예약의_날짜와_시간을_변경한다() {
        // given
        ReservationTime time = findTimeByStartAt("15:00");
        ReservationTime updateTime = findTimeByStartAt("12:00");
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        Long id = reservationRepository.insert(new Reservation(null, "브라운", date, time, theme));
        LocalDate updateDate = date.plusDays(1);

        Reservation updatedReservation = new Reservation(id, "브라운", updateDate, updateTime, theme);

        // when
        int updatedCount = reservationRepository.update(updatedReservation);

        // then
        Reservation result = reservationRepository.findById(id).get();
        assertAll(
                () -> assertThat(updatedCount).isEqualTo(1),
                () -> assertThat(result.getDate()).isEqualTo(updateDate),
                () -> assertThat(result.getTime().getId()).isEqualTo(updateTime.getId()),
                () -> assertThat(result.getTheme().getId()).isEqualTo(theme.getId()));
    }

    @Test
    void 날짜_시간_테마에_해당하는_예약이_존재하는지_확인한다() {
        // given
        ReservationTime time = findTimeByStartAt("12:00");
        ReservationTime otherTime = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        reservationRepository.insert(new Reservation(null, "브라운", date, time, theme));

        // when
        boolean exists = reservationRepository.existsWith(date, time.getId(), theme.getId());
        boolean notExistsWithOtherDate = reservationRepository.existsWith(date.plusDays(1), time.getId(), theme.getId());
        boolean notExistsWithOtherTime = reservationRepository.existsWith(date, otherTime.getId(), theme.getId());

        // then
        assertAll(
                () -> assertThat(exists).isTrue(),
                () -> assertThat(notExistsWithOtherDate).isFalse(),
                () -> assertThat(notExistsWithOtherTime).isFalse());
    }

    @Test
    void 예약_존재_여부를_락과_함께_확인한다() {
        // given
        ReservationTime time = findTimeByStartAt("12:00");
        ReservationTime otherTime = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        reservationRepository.insert(new Reservation(null, "브라운", date, time, theme));

        // when
        boolean exists = reservationRepository.existsWithForUpdate(date, time.getId(), theme.getId());
        boolean notExistsWithOtherDate = reservationRepository.existsWithForUpdate(date.plusDays(1), time.getId(), theme.getId());
        boolean notExistsWithOtherTime = reservationRepository.existsWithForUpdate(date, otherTime.getId(), theme.getId());

        // then
        assertAll(
                () -> assertThat(exists).isTrue(),
                () -> assertThat(notExistsWithOtherDate).isFalse(),
                () -> assertThat(notExistsWithOtherTime).isFalse());
    }

    @Test
    void 테마와_날짜에_해당하는_예약_목록을_조회한다() {
        // given
        ReservationTime time1 = findTimeByStartAt("15:00");
        ReservationTime time2 = findTimeByStartAt("12:00");
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        Theme otherTheme = new Theme(2L, "다른 테마 이름", "다른 테마 설명", "다른 썸네일");
        reservationRepository.insert(new Reservation(null, "브라운", date, time1, theme));
        reservationRepository.insert(new Reservation(null, "구구", date, time2, otherTheme));
        reservationRepository.insert(new Reservation(null, "포비", date.plusDays(1), time2, theme));

        // when
        List<Reservation> result = reservationRepository.findReservationsByThemeAndDate(theme.getId(), date);

        // then
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result).extracting(Reservation::getName)
                        .containsExactly("브라운"),
                () -> assertThat(result).extracting(Reservation::getDate)
                        .containsExactly(date),
                () -> assertThat(result).extracting(reservation -> reservation.getTheme().getId())
                        .containsExactly(theme.getId()));
    }

    @Test
    void 테마_id에_해당하는_예약이_존재하는지_확인한다() {
        // given
        ReservationTime time = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        Reservation reservation = new Reservation(null, "브라운", date, time, theme);
        reservationRepository.insert(reservation);

        // when
        boolean exists = reservationRepository.existsByThemeId(1L);
        boolean notExists = reservationRepository.existsByThemeId(999L);

        // then
        assertAll(
                () -> assertThat(exists).isTrue(),
                () -> assertThat(notExists).isFalse());
    }

    @Test
    void 시간_id에_해당하는_예약이_존재하는지_확인한다() {
        // given
        ReservationTime time = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        Reservation reservation = new Reservation(null, "브라운", date, time, theme);
        reservationRepository.insert(reservation);

        // when
        boolean exists = reservationRepository.existsByTimeId(time.getId());
        boolean notExists = reservationRepository.existsByTimeId(999L);

        // then
        assertAll(
                () -> assertThat(exists).isTrue(),
                () -> assertThat(notExists).isFalse());
    }

    private ReservationTime findTimeByStartAt(String startAt) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE start_at = ?;";
        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, rowNum) -> {
                    ReservationTime reservationTime = new ReservationTime(
                            resultSet.getLong("id"),
                            resultSet.getObject("start_at", LocalTime.class));
                    return reservationTime;
                }, startAt);
    }
}
