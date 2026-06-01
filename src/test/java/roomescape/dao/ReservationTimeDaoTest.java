package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.dao.dto.ReservationTimeAvailability;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class, SlotDao.class})
class ReservationTimeDaoTest {

    @Autowired
    private ReservationTimeDao timeDao;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private SlotDao slotDao;

    @Test
    void 예약_시간을_생성한다() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        ReservationTime reservationTime = new ReservationTime(startAt);

        // when
        ReservationTime savedReservationTime = timeDao.save(reservationTime);

        // then
        assertAll(
                () -> assertThat(savedReservationTime.getId()).isNotNull(),
                () -> assertThat(savedReservationTime.getStartAt()).isEqualTo(startAt)
        );
    }

    @Test
    void 예약_시간_목록을_조회한다() {
        // given
        saveReservationTime(LocalTime.of(10, 0));
        saveReservationTime(LocalTime.of(11, 0));
        saveReservationTime(LocalTime.of(12, 0));
        saveReservationTime(LocalTime.of(13, 0));
        saveReservationTime(LocalTime.of(14, 0));

        // when
        List<ReservationTime> reservationTimes = timeDao.findAll();

        // then
        assertAll(
                () -> assertThat(reservationTimes).hasSize(5),
                () -> assertThat(reservationTimes.get(0).getStartAt()).isEqualTo(LocalTime.of(10, 0))
        );
    }

    @Test
    void 아이디에_맞는_예약_시간을_조회한다() {
        // given
        ReservationTime reservationTime = saveReservationTime(LocalTime.of(10, 0));

        // when
        Optional<ReservationTime> selectReservationTime = timeDao.findById(reservationTime.getId());

        // then
        assertAll(
                () -> assertThat(selectReservationTime).isPresent(),
                () -> assertThat(selectReservationTime.get().getStartAt()).isEqualTo(reservationTime.getStartAt())
        );
    }

    @Test
    void 테마_아이디와_선택_날짜에_해당하는_예약_시간을_조회한다() {
        // given
        ReservationTime savedReservationTime = saveReservationTime(LocalTime.of(10, 0));
        ReservationTime otherReservationTime = saveReservationTime(LocalTime.of(11, 0));
        ReservationTime otherDateReservationTime = saveReservationTime(LocalTime.of(12, 0));

        Theme savedTheme = saveTheme("방탈출", "로지와 러키의 방탈출", "https:fsof/ommff");
        Theme otherTheme = saveTheme("공포방", "밤밤과 러로의 방탈출", "https:fsof/sdafjifdsmmff");

        LocalDate date = LocalDate.of(2026, 5, 5);
        saveReservation("러키", date, savedReservationTime, savedTheme);
        saveReservation("로지", date, otherReservationTime, otherTheme);
        saveReservation("러로", date.plusDays(1), otherDateReservationTime, savedTheme);

        // when
        List<ReservationTimeAvailability> reservationTimesOnCondition = timeDao.findAvailabilitiesByThemeIdAndDate(savedTheme.getId(), date);

        // then
        assertThat(reservationTimesOnCondition)
                .extracting(
                        ReservationTimeAvailability::startAt,
                        ReservationTimeAvailability::reserved
                )
                .containsExactly(
                        tuple(LocalTime.of(10, 0), true),
                        tuple(LocalTime.of(11, 0), false),
                        tuple(LocalTime.of(12, 0), false)
                );
    }

    @Test
    void 예약_시간이_존재하는지_확인한다() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);
        saveReservationTime(startAt);

        // when
        boolean result = timeDao.existsByStartAt(startAt);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 예약_시간을_삭제한다() {
        // given
        ReservationTime savedReservationTime = saveReservationTime(LocalTime.of(10, 0));

        // when
        timeDao.delete(savedReservationTime.getId());

        // then
        List<ReservationTime> reservationTimes = timeDao.findAll();
        assertThat(reservationTimes).isEmpty();
    }

    private ReservationTime saveReservationTime(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        return timeDao.save(reservationTime);
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        Theme theme = new Theme(name, description, thumbnail);
        return themeDao.save(theme);
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        Slot savedSlot = slotDao.save(new Slot(date, time, theme));
        Reservation reservation = new Reservation(savedSlot, name);
        return reservationDao.save(reservation);
    }
}
