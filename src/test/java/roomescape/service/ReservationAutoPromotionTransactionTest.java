package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;

@SpringBootTest
class ReservationAutoPromotionTransactionTest {

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private JpaReservationRepository reservationRepository;

    @MockitoSpyBean
    private JpaReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM reservation_waiting;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
    }

    @Test
    void 예약_취소_후_대기_승격_중_실패하면_전체_변경이_롤백된다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        Reservation reservation = reservationRepository.save(new Reservation(null, "브라운", date, time, theme));
        ReservationWaiting waiting = reservationWaitingRepository.save(
                new ReservationWaiting(null, "구구", date, time, theme));

        doThrow(new RuntimeException("대기 삭제 실패"))
                .when(reservationWaitingRepository)
                .delete(any(ReservationWaiting.class));

        // when & then
        assertThatThrownBy(() -> reservationService.delete(reservation.getId(), "브라운"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("대기 삭제 실패");

        assertThat(reservationRepository.findById(reservation.getId())).isPresent();
        assertThat(reservationRepository.findByName("구구")).isEmpty();
        assertThat(reservationWaitingRepository.findById(waiting.getId())).isPresent();
    }

    @Test
    void 같은_예약을_동시에_취소할_때_중복_승격이_발생하는지_관찰한다() throws Exception {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        Reservation reservation = reservationRepository.save(new Reservation(null, "브라운", date, time, theme));
        reservationWaitingRepository.save(new ReservationWaiting(null, "구구", date, time, theme));

        CountDownLatch bothReservationsLoaded = new CountDownLatch(2);
        AtomicInteger concurrentFindCount = new AtomicInteger();
        doAnswer(invocation -> {
            if (concurrentFindCount.incrementAndGet() <= 2) {
                bothReservationsLoaded.countDown();
                if (!bothReservationsLoaded.await(3, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("두 요청이 예약을 함께 조회하지 못했습니다.");
                }
            }
            return Optional.of(reservation);
        }).when(reservationRepository).findById(reservation.getId());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when
        Future<Throwable> first = executor.submit(() -> cancel(reservation.getId()));
        Future<Throwable> second = executor.submit(() -> cancel(reservation.getId()));

        Throwable firstFailure = first.get(5, TimeUnit.SECONDS);
        Throwable secondFailure = second.get(5, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        Integer promotedReservationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ?
                  AND time_id = ?
                  AND theme_id = ?
                """, Integer.class, date, time.getId(), theme.getId());
        Integer remainingWaitingCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservation_waiting
                WHERE date = ?
                  AND time_id = ?
                  AND theme_id = ?
                """, Integer.class, date, time.getId(), theme.getId());

        assertThat(promotedReservationCount).isEqualTo(1);
        assertThat(remainingWaitingCount).isZero();
        List<Throwable> failures = Stream.of(firstFailure, secondFailure)
                .filter(failure -> failure != null)
                .toList();
        assertThat(failures).hasSize(1);
        assertThat(failures.getFirst())
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.RESERVATION_OPERATION_CONFLICT));
    }

    private Throwable cancel(Long reservationId) {
        try {
            reservationService.delete(reservationId, "브라운");
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }
}
