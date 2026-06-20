package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.fixture.FixtureGenerator;

@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class, SlotDao.class, WaitingDao.class})
class ReservationDaoTest {

    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationTimeDao timeDao;
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
    void 예약을_생성한다() {
        // given
        ReservationTime savedReservationTime = fixture.saveReservationTime(LocalTime.of(10, 0));
        Theme savedTheme = fixture.saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");

        LocalDate date = LocalDate.of(2026, 5, 5);
        Slot savedSlot = fixture.saveSlot(date, savedReservationTime, savedTheme);
        Reservation reservation = new Reservation(savedSlot, "브라운");

        // when
        Reservation savedReservation = reservationDao.save(reservation);

        // then
        assertAll(
                () -> assertThat(savedReservation.getId()).isNotNull(),
                () -> assertThat(savedReservation.getName()).isEqualTo(reservation.getName()),
                () -> assertThat(savedReservation.getDate()).isEqualTo(reservation.getDate()),
                () -> assertThat(savedReservation.getTime()).isEqualTo(reservation.getTime()),
                () -> assertThat(savedReservation.getTheme()).isEqualTo(reservation.getTheme())
        );
    }

    @Test
    void 예약_목록을_조회한다() {
        // given
        ReservationTime time1 = fixture.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = fixture.saveReservationTime(LocalTime.of(11, 0));
        ReservationTime time3 = fixture.saveReservationTime(LocalTime.of(12, 0));
        ReservationTime time4 = fixture.saveReservationTime(LocalTime.of(13, 0));
        ReservationTime time5 = fixture.saveReservationTime(LocalTime.of(14, 0));
        Theme savedTheme = fixture.saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");

        LocalDate date = LocalDate.of(2026, 5, 5);

        fixture.saveReservation("브라운", date, time1, savedTheme);
        fixture.saveReservation("로지", date, time2, savedTheme);
        fixture.saveReservation("러키", date, time3, savedTheme);
        fixture.saveReservation("러로", date, time4, savedTheme);
        fixture.saveReservation("밤밤", date, time5, savedTheme);

        // when
        List<Reservation> reservations = reservationDao.findAll();

        // then
        assertAll(
                () -> assertThat(reservations).hasSize(5),
                () -> assertThat(reservations.getFirst().getName()).isEqualTo("브라운"),
                () -> assertThat(reservations.getFirst().getDate()).isEqualTo(date),

                () -> assertThat(reservations.getFirst().getTime().getId()).isEqualTo(time1.getId()),
                () -> assertThat(reservations.getFirst().getTime().getStartAt()).isEqualTo(time1.getStartAt()),

                () -> assertThat(reservations.getFirst().getTheme().getId()).isEqualTo(savedTheme.getId()),
                () -> assertThat(reservations.getFirst().getTheme().getName()).isEqualTo(savedTheme.getName()),
                () -> assertThat(reservations.getFirst().getTheme().getDescription()).isEqualTo(savedTheme.getDescription()),
                () -> assertThat(reservations.getFirst().getTheme().getThumbnail()).isEqualTo(savedTheme.getThumbnail())
        );
    }

    @Test
    void 이름에_따른_예약_목록을_조회한다() {
        // given
        ReservationTime time10 = fixture.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime time20 = fixture.saveReservationTime(LocalTime.of(20, 0));
        ReservationTime time22 = fixture.saveReservationTime(LocalTime.of(22, 0));

        Theme theme = fixture.saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");

        fixture.saveReservation("브라운", LocalDate.of(2026, 5, 5), time10, theme);
        fixture.saveReservation("브라운", LocalDate.of(2026, 5, 7), time20, theme);
        fixture.saveReservation("브라운", LocalDate.of(2026, 5, 7), time22, theme);

        fixture.saveReservation("로지", LocalDate.of(2026, 5, 8), time22, theme);
        fixture.saveReservation("러키", LocalDate.of(2026, 5, 9), time22, theme);

        // when
        List<Reservation> reservations = reservationDao.findAllByName("브라운");

        // then
        assertAll(
                () -> assertThat(reservations).hasSize(3),

                () -> assertThat(reservations)
                        .extracting(Reservation::getName)
                        .containsOnly("브라운"),

                () -> assertThat(reservations)
                        .extracting(
                                Reservation::getDate,
                                reservation -> reservation.getTime().getStartAt()
                        )
                        .containsExactly(
                                tuple(LocalDate.of(2026, 5, 7), LocalTime.of(22, 0)),
                                tuple(LocalDate.of(2026, 5, 7), LocalTime.of(20, 0)),
                                tuple(LocalDate.of(2026, 5, 5), LocalTime.of(10, 0))
                        )
        );
    }

    @Test
    void 테마와_날짜_및_시간이_일치하는_예약이_존재하는지_확인한다() {
        // given
        ReservationTime savedReservationTime = fixture.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime otherReservationTime = fixture.saveReservationTime(LocalTime.of(11, 0));

        Theme savedTheme = fixture.saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");
        Theme otherTheme = fixture.saveTheme("방탈출2", "밤밤과 러로의 방탈출", "https:Fasdg/dfgt");

        LocalDate date = LocalDate.of(2026, 5, 5);
        LocalDate otherDate = LocalDate.of(2026, 5, 6);

        fixture.saveReservation("브라운", date, savedReservationTime, savedTheme);

        // when & then
        assertAll(
                () -> assertThat(reservationDao.existsByThemeAndDateAndTime(
                        savedTheme.getId(),
                        date,
                        savedReservationTime.getId()
                )).isTrue(),

                () -> assertThat(reservationDao.existsByThemeAndDateAndTime(
                        otherTheme.getId(),
                        date,
                        savedReservationTime.getId()
                )).isFalse(),

                () -> assertThat(reservationDao.existsByThemeAndDateAndTime(
                        savedTheme.getId(),
                        otherDate,
                        savedReservationTime.getId()
                )).isFalse(),

                () -> assertThat(reservationDao.existsByThemeAndDateAndTime(
                        savedTheme.getId(),
                        date,
                        otherReservationTime.getId()
                )).isFalse()
        );
    }

    @Test
    void 자신의_예약이_아닌_다른_예약이_같은_테마_날짜_시간에_존재하면_중복이라_판단한다() {
        // given
        ReservationTime originalTime = fixture.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime alreadyReservedTime = fixture.saveReservationTime(LocalTime.of(20, 0));

        Theme originalTheme = fixture.saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");
        Theme alreadyReservedTheme = fixture.saveTheme("방탈출2", "밤밤과 러로의 방탈출", "https:fsof/sdafjifdsmmff");

        Reservation myReservation = fixture.saveReservation(
                "러키",
                LocalDate.of(2026, 5, 10),
                originalTime,
                originalTheme
        );

        fixture.saveReservation(
                "브라운",
                LocalDate.of(2026, 5, 12),
                alreadyReservedTime,
                alreadyReservedTheme
        );

        // when
        boolean exists = reservationDao.existsByThemeAndDateAndTimeAndIdNot(
                alreadyReservedTheme.getId(),
                LocalDate.of(2026, 5, 12),
                alreadyReservedTime.getId(),
                myReservation.getId()
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 자기_자신의_예약만_있으면_같은_테마_날짜_시간의_예약이_존재하지_않는다고_판단한다() {
        // given
        ReservationTime reservationTime = fixture.saveReservationTime(LocalTime.of(10, 0));
        Theme theme = fixture.saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");
        LocalDate date = LocalDate.of(2026, 5, 10);
        Reservation existReservation = fixture.saveReservation("브라운", date, reservationTime, theme);

        // when
        boolean exists = reservationDao.existsByThemeAndDateAndTimeAndIdNot(
                theme.getId(),
                date,
                reservationTime.getId(),
                existReservation.getId()
        );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 예약을_수정한다() {
        // given
        ReservationTime originalTime = fixture.saveReservationTime(LocalTime.of(10, 0));
        ReservationTime changedTime = fixture.saveReservationTime(LocalTime.of(20, 0));

        Theme originalTheme = fixture.saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");
        Theme changedTheme = fixture.saveTheme("방탈출2", "밤밤과 러로의 방탈출", "https:fsof/sdafjifdsmmff");

        Reservation savedReservation = fixture.saveReservation(
                "브라운",
                LocalDate.of(2026, 5, 5),
                originalTime,
                originalTheme
        );

        Slot changedSlot = fixture.saveSlot(LocalDate.of(2026, 5, 10), changedTime, changedTheme);
        Reservation changedReservation = new Reservation(
                savedReservation.getId(),
                changedSlot,
                "브라운",
                roomescape.domain.ReservationStatus.CONFIRMED
        );

        // when
        reservationDao.update(changedReservation);

        // then
        Reservation foundReservation = reservationDao.findById(savedReservation.getId()).get();

        assertThat(foundReservation)
                .extracting(
                        Reservation::getId,
                        Reservation::getName,
                        Reservation::getDate,
                        reservation -> reservation.getTime().getId(),
                        reservation -> reservation.getTime().getStartAt(),
                        reservation -> reservation.getTheme().getId(),
                        reservation -> reservation.getTheme().getName(),
                        reservation -> reservation.getTheme().getDescription(),
                        reservation -> reservation.getTheme().getThumbnail()
                )
                .containsExactly(
                        savedReservation.getId(),
                        "브라운",
                        LocalDate.of(2026, 5, 10),
                        changedTime.getId(),
                        changedTime.getStartAt(),
                        changedTheme.getId(),
                        changedTheme.getName(),
                        changedTheme.getDescription(),
                        changedTheme.getThumbnail()
                );
    }

    @Test
    void 예약을_삭제한다() {
        // given
        ReservationTime savedReservationTime = fixture.saveReservationTime(LocalTime.of(10, 0));
        Theme savedTheme = fixture.saveTheme("방탈출1", "로지와 러키의 방탈출", "https:fsof/ommff");
        LocalDate date = LocalDate.of(2026, 5, 5);

        Reservation savedReservation = fixture.saveReservation("예약1", date, savedReservationTime, savedTheme);

        // when
        reservationDao.delete(savedReservation.getId());

        // then
        List<Reservation> reservations = reservationDao.findAll();
        assertThat(reservations).hasSize(0);
    }
}
