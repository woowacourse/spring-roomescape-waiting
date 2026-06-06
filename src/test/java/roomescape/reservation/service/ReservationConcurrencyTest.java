package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(properties = {
        "spring.sql.init.data-locations=",
        "spring.datasource.url=jdbc:h2:mem:concurrency-test;DB_CLOSE_DELAY=-1"
})
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationWaitingRepository waitingRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member reserver;
    private Member waiter;
    private Member newcomer;
    private ReservationTime time;
    private Theme theme;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        reserver = memberRepository.save(Member.of("reserver", "reserver@test.com", "1234"));
        waiter = memberRepository.save(Member.of("waiter", "waiter@test.com", "1234"));
        newcomer = memberRepository.save(Member.of("newcomer", "newcomer@test.com", "1234"));
        time = timeRepository.save(ReservationTime.restore(null, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        theme = themeRepository.save(Theme.restore(null, "테마A", "설명A", "https://a.com"));
        futureDate = LocalDate.now().plusDays(1);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM reservation_time");
    }

    @Test
    @DisplayName("예약 취소와 같은 슬롯 신규 신청이 동시에 일어나도 예약은 1개로 유지되고 대기자가 누락되지 않는다")
    void 취소와_신규신청_동시_요청() throws InterruptedException {
        Reservation reservation = reservationRepository.save(Reservation.of(reserver, futureDate, time, theme));
        waitingRepository.save(ReservationWaiting.of(waiter, futureDate, time, theme));

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            await(start);
            reservationService.deleteReservation(reservation.getId(), reserver);
        });
        executor.submit(() -> {
            await(start);
            reservationService.createReservation(
                    newcomer, new ReservationRequest(futureDate, time.getId(), theme.getId()));
        });

        start.countDown();
        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        List<Reservation> slotReservations = reservationRepository.findByMemberId(waiter.getId());
        assertThat(slotReservations).hasSize(1);
        assertThat(slotReservations.get(0).getDate()).isEqualTo(futureDate);
        assertThat(waitingRepository.findByMemberId(newcomer.getId())).hasSize(1);
        assertThat(waitingRepository.findByMemberId(waiter.getId())).isEmpty();
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
