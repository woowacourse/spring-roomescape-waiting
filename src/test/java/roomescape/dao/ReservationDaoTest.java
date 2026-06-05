package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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

    private Theme theme;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        theme = themeDao.save(new Theme("테스트 테마", "설명", "/test"));
        time = reservationTimeDao.save(new ReservationTime(LocalTime.of(9, 0)));
    }

    @Test
    void ID로_예약_조회() {
        Reservation saved = reservationDao.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        Optional<Reservation> reservation = reservationDao.findById(saved.getId());

        assertThat(reservation)
                .map(Reservation::getId)
                .hasValue(saved.getId());
    }

    @Test
    void 이름으로_예약_조회() {
        Reservation saved = reservationDao.save(new Reservation("아나키", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        List<ReservationRank> reservation = reservationDao.findByName("아나키");

        assertThat(reservation.getFirst().getId()).isEqualTo(saved.getId());
    }

    @Test
    void 존재하지_않는_이름으로_조회하면_빈값_반환() {
        List<ReservationRank> reservation = reservationDao.findByName("없는이름");

        assertThat(reservation).isEmpty();
    }

    @Test
    void 이름으로_예약_조회_시_대기_순번_부여() {
        reservationDao.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));
        reservationDao.save(new Reservation("그해", LocalDate.now(), time, theme, ReservationStatus.WAITING));
        reservationDao.save(new Reservation("아나키", LocalDate.now(), time, theme, ReservationStatus.WAITING));

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
        reservationDao.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));
        Reservation waiting1 = reservationDao.save(new Reservation("그해", LocalDate.now(), time, theme, ReservationStatus.WAITING));
        reservationDao.save(new Reservation("아나키", LocalDate.now(), time, theme, ReservationStatus.WAITING));

        reservationDao.delete(waiting1.getId());

        ReservationRank secondWaiting = reservationDao.findByName("아나키").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();

        assertThat(secondWaiting.getRank()).isEqualTo(1);
    }

    @Test
    void 대기_예약_승인_확인() {
        LocalDate date = LocalDate.now();
        Reservation confirmed = reservationDao.save(new Reservation("브라운", date, time, theme, ReservationStatus.CONFIRMED));
        Reservation waiting = reservationDao.save(new Reservation("아나키", date, time, theme, ReservationStatus.WAITING));

        reservationDao.delete(confirmed.getId());
        reservationDao.update(date, theme.getId(), time.getId());

        Reservation result = reservationDao.findById(waiting.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void 예약_저장() {
        Reservation reservation = new Reservation("테스트", LocalDate.now().plusDays(1), time, theme,
                ReservationStatus.CONFIRMED);

        Reservation saved = reservationDao.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_삭제() {
        Reservation saved = reservationDao.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        reservationDao.delete(saved.getId());

        assertThat(reservationDao.findById(saved.getId())).isEmpty();
    }
}
