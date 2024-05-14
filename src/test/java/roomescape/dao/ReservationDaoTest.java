package roomescape.dao;

import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.reservation.NotFoundReservationException;

@SpringBootTest
class ReservationDaoTest {
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationTimeDao reservationTimeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private MemberDao memberDao;

    @AfterEach
    void tearDown() {
        reservationDao.deleteAll();
        reservationTimeDao.deleteAll();
        themeDao.deleteAll();
        memberDao.deleteAll();
    }

    @Test
    @DisplayName("예약 단권을 조회한다")
    void findById_ShouldGetSinglePersistence() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("name", "description", "thumbnail");
        Member member = new Member("memberName", "email", "password");
        ReservationTime savedTime = reservationTimeDao.save(time);
        Theme savedTheme = themeDao.save(theme);
        Member savedMember = memberDao.save(member);
        Reservation savedReservation = reservationDao.save(
                new Reservation(LocalDate.of(2023, FEBRUARY, 1), savedTime, savedTheme, savedMember));

        //when &then
        // then
        Assertions.assertThat(reservationDao.findById(savedReservation.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("예약을 저장한다")
    void save_ShouldSaveReservationPersistence() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("name", "description", "thumbnail");
        Member member = new Member("memberName", "email", "password");
        ReservationTime savedTime = reservationTimeDao.save(time);
        Theme savedTheme = themeDao.save(theme);
        Member savedMember = memberDao.save(member);

        // when
        reservationDao.save(
                new Reservation(LocalDate.of(2023, FEBRUARY, 1), savedTime, savedTheme, savedMember));

        // then
        Assertions.assertThat(reservationDao.findAll())
                .hasSize(1);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete_ShouldRemovePersistence() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("name", "description", "thumbnail");
        ReservationTime savedTime = reservationTimeDao.save(time);
        Theme savedTheme = themeDao.save(theme);
        Member member = new Member("a", "b", "c");
        Reservation savedReservation = reservationDao.save(
                new Reservation(LocalDate.of(2023, FEBRUARY, 1), savedTime, savedTheme, member));

        // when
        reservationDao.delete(savedReservation);

        // then
        Assertions.assertThat(reservationDao.findById(savedReservation.getId()))
                .isEmpty();
    }


    @Test
    @DisplayName("없는 예약에 대한 삭제 요청시 예외를 발생시킨다.")
    void remove_ShouldThrowException_WhenReservationDoesNotExist() {
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("name", "description", "thumbnail");
        ReservationTime savedTime = reservationTimeDao.save(time);
        Theme savedTheme = themeDao.save(theme);
        Reservation reservation = new Reservation(LocalDate.of(2023, FEBRUARY, 1), savedTime, savedTheme, null);

        // when & then
        Assertions.assertThatThrownBy(() -> reservationDao.delete(reservation))
                .isInstanceOf(NotFoundReservationException.class);
    }


    @Test
    @DisplayName("특정 시간대, 테마이름, 예약자명의 예약들을 조회할 수 있다.")
    void findByPeriodAndMemberAndTheme() {
        // given
        Theme theme1 = new Theme("theme_name", "desc", "thumbnail");
        Theme theme2 = new Theme("theme_name2", "desc", "thumbnail");
        Theme savedTheme1 = themeDao.save(theme1);
        Theme savedTheme2 = themeDao.save(theme2);

        ReservationTime time = new ReservationTime(LocalTime.of(1, 0));
        ReservationTime savedTime = reservationTimeDao.save(time);

        Member member1 = new Member("name1", "email", "password");
        Member member2 = new Member("name2", "email", "password");
        Member savedMember1 = memberDao.save(member1);
        Member savedMember2 = memberDao.save(member2);

        Reservation reservation1 = new Reservation(LocalDate.of(2023, JANUARY, 1), savedTime, savedTheme1,
                savedMember1);
        Reservation reservation2 = new Reservation(LocalDate.of(2023, JANUARY, 2), savedTime, savedTheme1,
                savedMember1);

        Reservation reservation3 = new Reservation(LocalDate.of(2023, JANUARY, 3), savedTime, savedTheme1,
                savedMember1);
        Reservation reservation4 = new Reservation(LocalDate.of(2023, JANUARY, 2), savedTime, savedTheme2,
                savedMember1);
        Reservation reservation5 = new Reservation(LocalDate.of(2022, DECEMBER, 31), savedTime, savedTheme1,
                savedMember1);
        Reservation reservation6 = new Reservation(LocalDate.of(2023, JANUARY, 1), savedTime, savedTheme1,
                savedMember2);
        Reservation savedReservation1 = reservationDao.save(reservation1);
        Reservation savedReservation2 = reservationDao.save(reservation2);
        reservationDao.save(reservation3);
        reservationDao.save(reservation4);
        reservationDao.save(reservation5);
        reservationDao.save(reservation6);

        // when
        List<Reservation> findReservations = reservationDao.findByPeriodAndMemberAndTheme(
                LocalDate.of(2023, JANUARY, 1), LocalDate.of(2023, JANUARY, 2),
                savedMember1.getName(), savedTheme1.getName());

        // then
        Assertions.assertThat(findReservations)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        savedReservation1, savedReservation2
                );
    }
}
