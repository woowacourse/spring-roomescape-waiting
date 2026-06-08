package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@JdbcTest
@Import({ReservationRepository.class, ReservationTimeRepository.class, ThemeRepository.class})
class ReservationRepositoryTest {

    private static final int DEFAULT_RESERVATION_COUNT = 21;
    private static final Long AVAILABLE_RESERVATION_ID = 1L;
    private static final Long AVAILABLE_TIME_ID = 1L;
    private static final Long AVAILABLE_THEME_ID = 1L;

    @Autowired
    private ReservationRepository reservationDao;

    @Autowired
    private ReservationTimeRepository reservationTimeDao;

    @Autowired
    private ThemeRepository themeDao;

    @Test
    void 전체_예약_조회() {
        List<Reservation> reservations = reservationDao.findAll();

        assertThat(reservations).hasSize(DEFAULT_RESERVATION_COUNT);
    }

    @Test
    void ID로_예약_조회() {
        Reservation reservation = reservationDao.findById(AVAILABLE_RESERVATION_ID);

        assertThat(reservation).isNotNull();
        assertThat(reservation.getId()).isEqualTo(AVAILABLE_RESERVATION_ID);
    }

    @Test
    void 이름으로_예약_조회() {
        List<Reservation> reservation = reservationDao.findByName("아나키");

        assertThat(reservation).hasSize(2);
    }

    @Test
    void 존재하지_않는_이름으로_조회하면_빈값_반환() {
        List<Reservation> reservation = reservationDao.findByName("없는이름");

        assertThat(reservation).isEmpty();
    }

    @Test
    void 슬롯_예약_조회_시_요청_시각_순으로_정렬() {
        LocalDate contestedDate = LocalDate.now().minusDays(6);

        List<Reservation> slot = reservationDao.findBySlot(contestedDate, AVAILABLE_TIME_ID, AVAILABLE_THEME_ID);

        assertThat(slot).extracting(Reservation::getName)
                .containsExactly("브라운", "그해", "아나키");
    }

    @Test
    void 대기_삭제_시_후순위_대기자가_앞으로_당겨짐() {
        LocalDate contestedDate = LocalDate.now().minusDays(6);
        reservationDao.delete(20L);

        List<Reservation> slot = reservationDao.findBySlot(contestedDate, AVAILABLE_TIME_ID, AVAILABLE_THEME_ID);

        assertThat(slot).extracting(Reservation::getName)
                .containsExactly("브라운", "아나키");
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
        Reservation reservation = new Reservation(
                "테스트",
                LocalDate.now().plusDays(1),
                time,
                theme,
                LocalDateTime.now());

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

        assertThatThrownBy(() -> reservationDao.findById(AVAILABLE_RESERVATION_ID))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
