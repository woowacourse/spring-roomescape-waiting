package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.domain.Reservation;
import roomescape.dto.projection.ReservationOrderProjection;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class})
class ReservationDaoTest {

    public static final int DEFALUT_RESERVATION_COUNT = 21;
    public static final Long AVAILABLE_RESERVATION_ID = 1L;
    public static final Long AVAILABLE_TIME_ID = 1L;
    public static final Long AVAILABLE_THEME_ID = 1L;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Test
    void 전체_예약_조회() {
        List<Reservation> reservations = reservationDao.findAll();

        assertThat(reservations).hasSize(DEFALUT_RESERVATION_COUNT);
    }

    @Test
    void ID로_예약_조회() {
        Reservation reservation = reservationDao.findById(AVAILABLE_RESERVATION_ID);

        assertThat(reservation).isNotNull();
        assertThat(reservation.getId()).isEqualTo(AVAILABLE_RESERVATION_ID);
    }

    @Test
    void 이름으로_예약_조회() {
        List<ReservationOrderProjection> reservation = reservationDao.findByName("아나키");

        assertThat(reservation).hasSize(2);
    }

    @Test
    void 존재하지_않는_이름으로_조회하면_빈값_반환() {
        List<ReservationOrderProjection> reservation = reservationDao.findByName("없는이름");

        assertThat(reservation).isEmpty();
    }

    @Test
    void 이름으로_예약_조회_시_대기_순번_부여() {
        ReservationOrderProjection firstWaiting = reservationDao.findByName("그해").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();
        ReservationOrderProjection secondWaiting = reservationDao.findByName("아나키").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();

        assertThat(firstWaiting.getOrder()).isEqualTo(1);
        assertThat(secondWaiting.getOrder()).isEqualTo(2);
    }

    @Test
    void 대기_삭제_시_후순위_대기자_순번_재정렬() {
        reservationDao.delete(20L);

        ReservationOrderProjection secondWaiting = reservationDao.findByName("아나키").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();

        assertThat(secondWaiting.getOrder()).isEqualTo(1);
    }

    @Test
    void 예약_존재_여부_확인_존재하는_경우() {
        ReservationTime time = reservationTimeDao.findTimeById(AVAILABLE_TIME_ID);
        Theme theme = themeDao.findThemeById(AVAILABLE_THEME_ID);
        LocalDate date = LocalDate.now().minusDays(6);

        boolean exists = reservationDao.existsBy(date, theme, time);

        assertThat(exists).isTrue();
    }

    @Test
    void 예약_존재_여부_확인_존재하지_않는_경우() {
        ReservationTime time = reservationTimeDao.findTimeById(AVAILABLE_TIME_ID);
        Theme theme = themeDao.findThemeById(AVAILABLE_THEME_ID);
        LocalDate date = LocalDate.now().plusDays(10);

        boolean exists = reservationDao.existsBy(date, theme, time);
        assertThat(exists).isFalse();
    }

    @Test
    void 예약_저장() {
        ReservationTime time = reservationTimeDao.findTimeById(AVAILABLE_TIME_ID);
        Theme theme = themeDao.findThemeById(AVAILABLE_THEME_ID);
        Reservation reservation = new Reservation("테스트", LocalDate.now().plusDays(1), time, theme,
                ReservationStatus.CONFIRMED);

        Reservation saved = reservationDao.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_날짜_시간_변경() {
        LocalDate newDate = LocalDate.now().plusDays(5);
        Long newTimeId = 2L;

        Reservation updated = reservationDao.update(AVAILABLE_RESERVATION_ID, newDate, newTimeId);

        assertThat(updated.getDate()).isEqualTo(newDate);
        assertThat(updated.getTime().getId()).isEqualTo(newTimeId);
    }

    @Test
    void 예약_삭제() {
        reservationDao.delete(AVAILABLE_RESERVATION_ID);

        assertThatThrownBy(() -> reservationDao.findById(AVAILABLE_RESERVATION_ID)).isInstanceOf(EmptyResultDataAccessException.class);
    }
}
