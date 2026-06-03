package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.dto.ReservationRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@SpringBootTest
public class ReserveOrWaitRaceConditionTest {

    private static final LocalDate FUTURE_FIRST_DATE = LocalDate.now().plusDays(1);
    private static final LocalDateTime WAITLIST_CREATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationTimeRepository timeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private WaitlistRepository waitlistRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void 동시에_같은_빈_슬롯을_예약하면_하나는_예약되고_나머지는_대기된다() throws InterruptedException {
        int requestCount = 5;
        List<ReservationRequest> requests = createSameSlotReservationRequests(
            requestCount,
            "브리", "네오", "포비", "검프", "준"
        );

        ExecutorService executor = Executors.newFixedThreadPool(requestCount);

        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);

        Queue<ReservationWithStatus> results = new ConcurrentLinkedQueue<>();
        Queue<Throwable> exceptions = new ConcurrentLinkedQueue<>();

        for (ReservationRequest request : requests) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    ReservationWithStatus reservationWithStatus = reservationService.reserveOrWait(request);

                    results.add(reservationWithStatus);
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        executor.shutdown();

        long reservedCount = results.stream()
            .filter(result -> result.getStatus() == ReservationStatus.RESERVED)
            .count();

        long waitingCount = results.stream()
            .filter(result -> result.getStatus() == ReservationStatus.WAITING)
            .count();

        assertThat(exceptions).isEmpty();
        assertThat(results).hasSize(requestCount);
        assertThat(reservedCount).isEqualTo(1);
        assertThat(waitingCount).isEqualTo(requestCount - 1);
    }

    @Test
    void unique_충돌이_발생하면_별도_트랜잭션으로_대기_등록한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        Reservation firstReservation = createReservation("브라운", reservationTime, theme);
        Reservation duplicateReservation = createReservation("브리", reservationTime, theme);

        reservationRepository.save(firstReservation);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(
            status -> reservationRepository.save(duplicateReservation)
        )).isInstanceOf(DataIntegrityViolationException.class);

        Long waitlistId = transactionTemplate.execute(
            status -> waitlistRepository.save(duplicateReservation, WAITLIST_CREATED_AT)
        );

        Waitlist savedWaitlist = waitlistRepository.findById(waitlistId).orElseThrow();
        assertThat(savedWaitlist.getName()).isEqualTo("브리");
        assertThat(savedWaitlist.getDate()).isEqualTo(FUTURE_FIRST_DATE);
        assertThat(savedWaitlist.getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(savedWaitlist.getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 같은_트랜잭션에서_unique_충돌을_잡고_대기_등록할_때_H2_JdbcTemplate환경에서_확인한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        Reservation firstReservation = createReservation("브라운", reservationTime, theme);
        Reservation duplicateReservation = createReservation("브리", reservationTime, theme);

        reservationRepository.save(firstReservation);

        Long waitlistId = transactionTemplate.execute(status -> {
            try {
                reservationRepository.save(duplicateReservation);
            } catch (DataIntegrityViolationException e) {
                return waitlistRepository.save(duplicateReservation, WAITLIST_CREATED_AT);
            }

            throw new AssertionError("같은 슬롯 예약 저장은 unique 충돌이 발생해야 합니다.");
        });

        Waitlist savedWaitlist = waitlistRepository.findById(waitlistId).orElseThrow();
        assertThat(savedWaitlist.getName()).isEqualTo("브리");
        assertThat(savedWaitlist.getDate()).isEqualTo(FUTURE_FIRST_DATE);
        assertThat(savedWaitlist.getTime().getId()).isEqualTo(reservationTime.getId());
        assertThat(savedWaitlist.getTheme().getId()).isEqualTo(theme.getId());
    }


    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime);
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme);
        return new Theme(
            id,
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnailImageUrl()
        );
    }

    private Reservation createReservation(String name, ReservationTime reservationTime, Theme theme) {
        return new Reservation(
            name,
            FUTURE_FIRST_DATE,
            reservationTime,
            theme
        );
    }

    private List<ReservationRequest> createSameSlotReservationRequests(int requestSize, String... names) {
        List<ReservationRequest> reservationRequests = new ArrayList<>();
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        for (int i = 0; i < requestSize; i++) {
            reservationRequests.add(new ReservationRequest(
                names[i],
                FUTURE_FIRST_DATE,
                reservationTime.getId(),
                theme.getId()
            ));
        }

        return reservationRequests;
    }
}
