package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRank;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class})
class ReservationDaoTest {

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 전체_예약_조회() {
        List<Reservation> reservations = reservationDao.findAll();

        assertThat(reservations).hasSize(21);
    }

    @Test
    void ID로_예약_조회() {
        Optional<Reservation> reservation = reservationDao.findById(1L);

        assertThat(reservation)
                .map(Reservation::getId)
                .hasValue(1L);
    }

    @Test
    void 이름으로_예약_조회() {
        List<ReservationRank> reservation = reservationDao.findByName("아나키");

        assertThat(reservation).hasSize(2);
    }

    @Test
    void 존재하지_않는_이름으로_조회하면_빈값_반환() {
        List<ReservationRank> reservation = reservationDao.findByName("없는이름");

        assertThat(reservation).isEmpty();
    }

    @Test
    void 이름으로_예약_조회_시_대기_순번_부여() {
        ReservationRank firstWaiting = reservationDao.findByName("그해").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();
        ReservationRank secondWaiting = reservationDao.findByName("아나키").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();

        assertThat(firstWaiting.getRank()).isEqualTo(1);
        assertThat(secondWaiting.getRank()).isEqualTo(2);
    }

    @Test
    void 대기_삭제_시_후순위_대기자_순번_재정렬() {
        reservationDao.delete(20L);

        ReservationRank secondWaiting = reservationDao.findByName("아나키").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();

        assertThat(secondWaiting.getRank()).isEqualTo(1);
    }

    @Test
    void 예약_존재_여부_확인_존재하는_경우() {
        Optional<ReservationTime> time = reservationTimeDao.findTimeById(1L);
        Optional<Theme> theme = themeDao.findThemeById(1L);
        LocalDate date = LocalDate.now().minusDays(6);

        boolean exists = reservationDao.existsByDateAndThemeAndTime(date, theme.get(), time.get());

        assertThat(exists).isTrue();
    }

    @Test
    void 예약_존재_여부_확인_존재하지_않는_경우() {
        Optional<ReservationTime> time = reservationTimeDao.findTimeById(1L);
        Optional<Theme> theme = themeDao.findThemeById(1L);
        LocalDate date = LocalDate.now().plusDays(10);

        boolean exists = reservationDao.existsByDateAndThemeAndTime(date, theme.get(), time.get());
        assertThat(exists).isFalse();
    }

    @Test
    void 예약_저장() {
        Optional<ReservationTime> time = reservationTimeDao.findTimeById(1L);
        Optional<Theme> theme = themeDao.findThemeById(1L);
        Reservation reservation = new Reservation("테스트", LocalDate.now().plusDays(1), time.get(), theme.get(),
                ReservationStatus.CONFIRMED);

        Reservation saved = reservationDao.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_삭제() {
        reservationDao.delete(1L);

        assertThat(reservationDao.findById(1L)).isEmpty();
    }
}
