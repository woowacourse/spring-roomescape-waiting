package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
class ReservationDaoTest {
    private final ReservationDao reservationDao;

    @Autowired
    public ReservationDaoTest(final JdbcTemplate jdbcTemplate) {
        this.reservationDao = new ReservationDao(jdbcTemplate);
    }

    private long getItemSize() {
        return reservationDao.findAll().size();
    }

    @DisplayName("Db에 등록된 모든 예약 목록을 조회한다.")
    @Test
    void given_when_findAll_then_returnReservations() {
        //given, when, then
        assertThat(reservationDao.findAll().size()).isEqualTo(7);
    }

    @DisplayName("Db에 예약 정보를 저장한다.")
    @Test
    void given_reservation_when_create_then_returnCreatedReservationId() {
        //given
        Reservation reservation = new Reservation(
                new Member(1L, "poke@test.com", "poke", "role"),
                LocalDate.parse("2099-01-11"),
                new TimeSlot(1L, LocalTime.parse("10:00")),
                new Theme(1L, "name", "description", "thumbnail"));
        //when, then
        assertThat(reservationDao.create(reservation)).isEqualTo(8);
    }

    @DisplayName("예약 id로 Db에서 예약 정보를 삭제한다.")
    @Test
    void given_when_delete_then_deletedFromDb() {
        //given
        long initialSize = getItemSize();
        //when
        reservationDao.delete(1L);
        long afterSize = getItemSize();
        //then
        assertThat(afterSize).isEqualTo(initialSize - 1);
    }

    @DisplayName("예약 날짜, 시간Id, 테마Id를 통해 예약여부를 확인할 수 있다.")
    @Test
    void given_when_isExist_then_getExistResult() {
        //given, when, then
        assertThat(reservationDao.isExists(LocalDate.parse("2024-05-01"), 3L, 2L)).isTrue();
    }

    @DisplayName("시간 Id로 등록한 예약이 존재하는지 확인할 수 있다.")
    @Test
    void given_when_isExistTimeId_then_getExistResult() {
        //given, when, then
        assertThat(reservationDao.isExistsTimeId(1L)).isTrue();
    }

    @DisplayName("테마 Id로 등록한 예약이 존재하는지 확인할 수 있다.")
    @Test
    void given_when_isExistThemeId_then_getExistResult() {
        //given, when, then
        assertThat(reservationDao.isExistsThemeId(1L)).isTrue();
    }

    @Test
    @DisplayName("memberId, themeId, 기간을 이용하여 예약을 조회할 수 있다.")
    void given_memberIdThemeIdAndPeriod_when_find_then_Reservations() {
        //given
        Long themeId = 2L;
        Long memberId = 1L;
        LocalDate dateFrom = LocalDate.parse("2024-04-30");
        LocalDate dateTo = LocalDate.parse("2024-05-01");
        //when, then
        final List<Reservation> reservations = reservationDao.find(themeId, memberId, dateFrom, dateTo);
        assertThat(reservations.size()).isEqualTo(3);
    }
}