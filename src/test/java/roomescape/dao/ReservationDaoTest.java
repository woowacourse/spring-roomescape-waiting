package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.AvailableReservationTimeSearch;
import roomescape.dto.reservation.ReservationExistenceCheck;
import roomescape.dto.reservation.ReservationFilterParam;

import java.time.LocalDate;
import java.util.List;


class ReservationDaoTest extends DaoTest {

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationDao reservationDao;

    private Member member;
    private ReservationTime reservationTime;
    private Theme theme;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        member = memberDao.save(MEMBER_BROWN());
        reservationTime = reservationTimeDao.save(RESERVATION_TIME_SIX());
        theme = themeDao.save(THEME_HORROR());
        reservation = reservationDao.save(new Reservation(member, DATE_MAY_EIGHTH, reservationTime, theme));
    }

    @Test
    @DisplayName("예약을 저장한다.")
    void save() {
        // given
        final Reservation reservation = new Reservation(member, DATE_MAY_NINTH, reservationTime, theme);

        // when
        final Reservation actual = reservationDao.save(reservation);

        // then
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // when
        final List<Reservation> actual = reservationDao.findAll();

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("검색 조건에 따른 예약 목록을 조회한다.")
    void findAllByFilterParameter() {
        // given
        final ReservationFilterParam reservationFilterParam = new ReservationFilterParam(
                theme.getId(), member.getId(), LocalDate.parse(DATE_MAY_EIGHTH), LocalDate.parse(DATE_MAY_NINTH));

        // when
        final List<Reservation> actual = reservationDao.findAllBy(reservationFilterParam);

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("동일 시간대의 예약 목록을 조회한다.")
    void findAllByDateAndTime() {
        // given
        final ReservationExistenceCheck reservationExistenceCheck
                = new ReservationExistenceCheck(LocalDate.parse(DATE_MAY_EIGHTH), reservationTime.getId(), theme.getId());

        // when
        final List<Reservation> actual = reservationDao.findAllBy(reservationExistenceCheck);

        // then
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("Id에 해당하는 예약이 존재하면 true를 반환한다.")
    void returnTrueWhenExistById() {
        // when
        final boolean actual = reservationDao.existById(reservation.getId());

        // then
        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("Id에 해당하는 예약이 존재하지 않으면 false를 반환한다.")
    void returnFalseWhenNotExistById() {
        // given
        final Long id = 2L;

        // when
        final boolean actual = reservationDao.existById(id);

        // then
        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("Id에 해당하는 예약을 삭제한다.")
    void deleteById() {
        // when
        reservationDao.deleteById(reservation.getId());

        // then
        final List<Reservation> actual = reservationDao.findAll();
        assertThat(actual).hasSize(0);
    }

    @Test
    @DisplayName("timeId에 해당하는 예약 건수를 조회한다.")
    void countByTimeId() {
        // given
        final long timeId = 2L;

        // when
        final int actual = reservationDao.countByTimeId(timeId);

        // then
        assertThat(actual).isEqualTo(0);
    }

    @Test
    @DisplayName("예약 가능한 시간 목록을 조회한다.")
    void findAllByDateAndThemeId() {
        // given
        final AvailableReservationTimeSearch availableReservationTimeSearch
                = new AvailableReservationTimeSearch(LocalDate.parse(DATE_MAY_EIGHTH), theme.getId());

        // when
        final List<Long> actual = reservationDao.findTimeIds(availableReservationTimeSearch);

        // then
        assertThat(actual).hasSize(1);
    }
}
