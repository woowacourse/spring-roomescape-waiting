package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    private ReservationRepository reservationDao;

    @Autowired
    private ReservationTimeRepository reservationTimeDao;

    @Autowired
    private ThemeRepository themeDao;

    private Theme theme;

    @BeforeEach
    void setUp() {
        theme = themeDao.save(new Theme(null, "테마", "설명", "/url"));
    }

    private ReservationTime saveTime(LocalTime startAt) {
        return reservationTimeDao.save(new ReservationTime(startAt));
    }

    private Reservation saveReservation(String name, LocalDate date, ReservationTime time, LocalDateTime requestedAt) {
        return reservationDao.save(new Reservation(name, date, time, theme, requestedAt));
    }

    @Test
    void 전체_예약_조회() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        saveReservation("브라운", LocalDate.now().plusDays(1), time, LocalDateTime.now());
        saveReservation("브라운", LocalDate.now().plusDays(2), time, LocalDateTime.now());

        assertThat(reservationDao.findAll()).hasSize(2);
    }

    @Test
    void ID로_예약_조회() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, LocalDateTime.now());

        Reservation found = reservationDao.findById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("브라운");
    }

    @Test
    void 이름으로_예약_조회() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        saveReservation("아나키", LocalDate.now().plusDays(1), time, LocalDateTime.now());
        saveReservation("아나키", LocalDate.now().plusDays(2), time, LocalDateTime.now());
        saveReservation("브라운", LocalDate.now().plusDays(3), time, LocalDateTime.now());

        assertThat(reservationDao.findByName("아나키")).hasSize(2);
    }

    @Test
    void 존재하지_않는_이름으로_조회하면_빈값_반환() {
        assertThat(reservationDao.findByName("없는이름")).isEmpty();
    }

    @Test
    void 슬롯_예약_조회_시_요청_시각_순으로_정렬() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.now().plusDays(50);
        LocalDateTime base = LocalDateTime.now();

        saveReservation("브라운", date, time, base);
        saveReservation("그해", date, time, base.plusSeconds(1));
        saveReservation("아나키", date, time, base.plusSeconds(2));

        List<Reservation> slot = reservationDao.findBySlot(date, time.getId(), theme.getId());

        assertThat(slot).extracting(Reservation::getName)
                .containsExactly("브라운", "그해", "아나키");
    }

    @Test
    void 대기_삭제_시_후순위_대기자가_앞으로_당겨짐() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.now().plusDays(50);
        LocalDateTime base = LocalDateTime.now();
        saveReservation("브라운", date, time, base);
        Reservation deleteReservation = saveReservation("그해", date, time, base.plusSeconds(1));
        saveReservation("아나키", date, time, base.plusSeconds(2));

        assertThat(reservationDao.findBySlot(date, time.getId(), theme.getId()))
                .extracting(Reservation::getName)
                .containsExactly("브라운", "그해", "아나키");

        reservationDao.delete(deleteReservation.getId());

        assertThat(reservationDao.findBySlot(date, time.getId(), theme.getId()))
                .extracting(Reservation::getName)
                .containsExactly("브라운", "아나키");
    }

    @Test
    void 예약_존재_여부_확인_존재하는_경우() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation("브라운", date, time, LocalDateTime.now());

        assertThat(reservationDao.existsBy(date, theme, time)).isTrue();
    }

    @Test
    void 예약_존재_여부_확인_존재하지_않는_경우() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.now().plusDays(10);

        assertThat(reservationDao.existsBy(date, theme, time)).isFalse();
    }

    @Test
    void 예약_저장() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));

        Reservation saved = saveReservation("테스트", LocalDate.now().plusDays(1), time, LocalDateTime.now());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_날짜_시간_변경() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        ReservationTime newTime = saveTime(LocalTime.of(11, 0));
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, LocalDateTime.now());
        LocalDate newDate = LocalDate.now().plusDays(5);

        Reservation updated = reservationDao.update(saved.getId(), newDate, newTime.getId());

        assertThat(updated.getDate()).isEqualTo(newDate);
        assertThat(updated.getTime().getId()).isEqualTo(newTime.getId());
    }

    @Test
    void 예약_삭제() {
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        Reservation saved = saveReservation("브라운", LocalDate.now().plusDays(1), time, LocalDateTime.now());

        reservationDao.delete(saved.getId());

        assertThatThrownBy(() -> reservationDao.findById(saved.getId()))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
