package roomescape.reservation.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.response.ReservationTimeInfoResponse;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({ReservationTimeDao.class, ThemeDao.class, ReservationDao.class, MemberDao.class})
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationTimeDaoTest {

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private ReservationDao reservationDao;

    @Test
    @DisplayName("등록된 시간의 id를 통해 단건 조회할 수 있다.")
    void findTimeById() {
        //given
        reservationTimeDao.insert(new ReservationTime(1L, LocalTime.of(17, 30)));

        // when
        ReservationTime foundReservationTime = reservationTimeDao.findById(1L);

        // then
        assertThat(foundReservationTime.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("전체 시간 정보를 조회한다.")
    void readDbTimes() {
        // given
        reservationTimeDao.insert(new ReservationTime(LocalTime.of(17, 30)));
        reservationTimeDao.insert(new ReservationTime(LocalTime.of(19, 30)));

        // when
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();

        // then
        assertThat(reservationTimes.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("하나의 시간만 등록한 경우, DB를 조회 했을 때 조회 결과 개수는 1개이다.")
    void postTimeIntoDb() {
        // given
        reservationTimeDao.insert(new ReservationTime(1L, LocalTime.of(17, 30)));

        // when
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();

        // then
        assertThat(reservationTimes.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("하나의 시간만 등록한 경우, 시간 삭제 뒤 DB를 조회 했을 때 조회 결과 개수는 0개이다.")
    void readTimesSizeFromDbAfterPostAndDelete() {
        // given
        reservationTimeDao.insert(new ReservationTime(1L, LocalTime.of(17, 30)));

        // when
        reservationTimeDao.deleteById(1L);
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();

        // then
        assertThat(reservationTimes.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("테마ID와 날짜를 통해 예약 정보(전체 시간대, 예약 여부)를 조회한다.")
    void readReservationsByThemeIdAndDate() {
        // given
        ReservationTime reservationTime1 = reservationTimeDao.insert(new ReservationTime(LocalTime.of(17, 30)));
        ReservationTime reservationTime2 = reservationTimeDao.insert(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeDao.insert(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberDao.insert(new Member("name", "email@email.com", "password", Role.MEMBER));

        reservationDao.insert(new Reservation(
                LocalDate.of(2024, 4, 25), reservationTime1, theme, member
        ));
        reservationDao.insert(new Reservation(
                LocalDate.of(2024, 4, 26), reservationTime1, theme, member
        ));

        // when
        List<ReservationTimeInfoResponse> reservedTimeInfos = reservationTimeDao.findByDateAndThemeId(
                LocalDate.of(2024, 4, 25),
                theme.getId()
        ).reservationTimes();

        // then
        Assertions.assertAll(
                () -> assertThat(reservedTimeInfos.size()).isEqualTo(2),
                () -> assertThat(reservedTimeInfos.get(0).timeId()).isEqualTo(reservationTime1.getId()),
                () -> assertThat(reservedTimeInfos.get(0).alreadyBooked()).isTrue(),
                () -> assertThat(reservedTimeInfos.get(1).timeId()).isEqualTo(reservationTime2.getId()),
                () -> assertThat(reservedTimeInfos.get(1).alreadyBooked()).isFalse()
        );
    }
}
