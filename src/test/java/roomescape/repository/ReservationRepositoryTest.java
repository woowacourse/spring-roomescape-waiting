package roomescape.repository;

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
@Import({ReservationRepository.class, ReservationTimeRepository.class, ThemeRepository.class})
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Theme theme;
    private ReservationTime time;

    @BeforeEach
    void setUp() {
        theme = themeRepository.save(new Theme("테스트 테마", "설명", "/test"));
        time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
    }

    @Test
    void ID로_예약_조회() {
        Reservation saved = reservationRepository.save(
                new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        Optional<Reservation> reservation = reservationRepository.findById(saved.getId());

        assertThat(reservation)
                .map(Reservation::getId)
                .hasValue(saved.getId());
    }

    @Test
    void 이름으로_예약_조회() {
        Reservation saved = reservationRepository.save(
                new Reservation("아나키", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        List<ReservationRank> reservation = reservationRepository.findByName("아나키");

        assertThat(reservation.getFirst().getId()).isEqualTo(saved.getId());
    }

    @Test
    void 존재하지_않는_이름으로_조회하면_빈값_반환() {
        List<ReservationRank> reservation = reservationRepository.findByName("없는이름");

        assertThat(reservation).isEmpty();
    }

    @Test
    void 이름으로_예약_조회_시_대기_순번_부여() {
        reservationRepository.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));
        reservationRepository.save(new Reservation("그해", LocalDate.now(), time, theme, ReservationStatus.WAITING));
        reservationRepository.save(new Reservation("아나키", LocalDate.now(), time, theme, ReservationStatus.WAITING));

        ReservationRank firstWaiting = reservationRepository.findByName("그해").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();
        ReservationRank secondWaiting = reservationRepository.findByName("아나키").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();

        assertThat(firstWaiting.getRank()).isEqualTo(1);
        assertThat(secondWaiting.getRank()).isEqualTo(2);
    }

    @Test
    void 대기_삭제_시_후순위_대기자_순번_재정렬() {
        reservationRepository.save(new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));
        Reservation firstWaiting = reservationRepository.save(
                new Reservation("그해", LocalDate.now(), time, theme, ReservationStatus.WAITING));
        reservationRepository.save(new Reservation("아나키", LocalDate.now(), time, theme, ReservationStatus.WAITING));

        reservationRepository.delete(firstWaiting.getId());

        ReservationRank secondWaiting = reservationRepository.findByName("아나키").stream()
                .filter(r -> r.getStatus() == ReservationStatus.WAITING)
                .findFirst()
                .orElseThrow();

        assertThat(secondWaiting.getRank()).isEqualTo(1);
    }

    @Test
    void 결제_정보_업데이트_확인() {
        Reservation reservation = reservationRepository.save(
                new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.PENDING_PAYMENT));

        reservationRepository.updatePayment(reservation.getId(), "payment-key", ReservationStatus.CONFIRMED, "order-id",
                50000L);

        Reservation result = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(result.getPaymentKey()).isEqualTo("payment-key");
        assertThat(result.getOrderId()).isEqualTo("order-id");
        assertThat(result.getAmount()).isEqualTo(50000L);
    }

    @Test
    void 예약_저장() {
        Reservation reservation = new Reservation("테스트", LocalDate.now().plusDays(1), time, theme,
                ReservationStatus.CONFIRMED);

        Reservation saved = reservationRepository.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_삭제() {
        Reservation saved = reservationRepository.save(
                new Reservation("브라운", LocalDate.now(), time, theme, ReservationStatus.CONFIRMED));

        reservationRepository.delete(saved.getId());

        assertThat(reservationRepository.findById(saved.getId())).isEmpty();
    }
}
