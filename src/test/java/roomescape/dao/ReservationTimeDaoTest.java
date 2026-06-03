package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.dao.dto.ReservationTimeAvailability;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.fixture.FixtureGenerator;

@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class, SlotDao.class, WaitingDao.class})
class ReservationTimeDaoTest {

    @Autowired
    private ReservationTimeDao timeDao;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private SlotDao slotDao;
    @Autowired
    private WaitingDao waitingDao;

    private FixtureGenerator fixture;

    @BeforeEach
    void setUp() {
        fixture = new FixtureGenerator(themeDao, timeDao, slotDao, reservationDao, waitingDao);
    }

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
        fixture.saveReservationTime(LocalTime.of(10, 0));
        fixture.saveReservationTime(LocalTime.of(11, 0));
        fixture.saveReservationTime(LocalTime.of(12, 0));
        fixture.saveReservationTime(LocalTime.of(13, 0));
        fixture.saveReservationTime(LocalTime.of(14, 0));

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
        ReservationTime reservationTime = fixture.saveReservationTime(LocalTime.of(10, 0));

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
        ReservationTime savedReservationTime = fixture.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime otherReservationTime = fixture.saveReservationTime(LocalTime.of(11, 0));
        ReservationTime otherDateReservationTime = fixture.saveReservationTime(LocalTime.of(12, 0));

        Theme savedTheme = fixture.saveTheme("방탈출", "로지와 러키의 방탈출", "https:fsof/ommff");
        Theme otherTheme = fixture.saveTheme("공포방", "밤밤과 러로의 방탈출", "https:fsof/sdafjifdsmmff");

        LocalDate date = LocalDate.of(2026, 5, 5);
        fixture.saveReservation("러키", date, savedReservationTime, savedTheme);
        fixture.saveReservation("로지", date, otherReservationTime, otherTheme);
        fixture.saveReservation("러로", date.plusDays(1), otherDateReservationTime, savedTheme);

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
        fixture.saveReservationTime(startAt);

        // when
        boolean result = timeDao.existsByStartAt(startAt);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 예약_시간을_삭제한다() {
        // given
        ReservationTime savedReservationTime = fixture.saveReservationTime(LocalTime.of(10, 0));

        // when
        timeDao.delete(savedReservationTime.getId());

        // then
        List<ReservationTime> reservationTimes = timeDao.findAll();
        assertThat(reservationTimes).isEmpty();
    }
}
