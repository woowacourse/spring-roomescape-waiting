package roomescape.reservation.dao;

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
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class, MemberDao.class})
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationDaoTest {

    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationTimeDao reservationTimeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private MemberDao memberDao;


    @Test
    @DisplayName("전체 예약 정보를 조회한다")
    void readDbReservations() {
        // given
        ReservationTime reservationTime = reservationTimeDao.insert(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeDao.insert(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberDao.insert(new Member("name", "email@email.com", "password", Role.MEMBER));


        reservationDao.insert(new Reservation(
                LocalDate.of(2024, 4, 25),
                reservationTime,
                theme,
                member
        ));

        // when
        List<Reservation> reservations = reservationDao.findAll();

        // then
        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("하나의 예약만 등록한 경우, 예약 삭제 뒤 DB를 조회 했을 때 조회 결과 개수는 0개이다.")
    void readReservationsSizeFromDbAfterPostAndDelete() {
        // given
        ReservationTime reservationTime = reservationTimeDao.insert(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeDao.insert(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberDao.insert(new Member("name", "email@email.com", "password", Role.MEMBER));

        // when
        Reservation savedReservation = reservationDao.insert(new Reservation(
                LocalDate.of(2024, 4, 25),
                reservationTime,
                theme,
                member
        ));
        int deleteCount = reservationDao.deleteById(savedReservation.getId());

        // then
        assertThat(deleteCount).isEqualTo(1);
    }
}
