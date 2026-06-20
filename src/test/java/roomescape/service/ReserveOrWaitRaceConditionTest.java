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
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.config.TestClockConfig;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.dto.ReservationRequest;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@SpringBootTest
@Import(TestClockConfig.class)
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
    private SlotRepository slotRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private MemberRepository memberRepository;

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
    void 이미_예약된_슬롯에_동시에_대기를_신청하면_응답_순번이_중복되지_않는다() throws InterruptedException {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        reservationService.reserveOrWait(new ReservationRequest(
            "브라운",
            FUTURE_FIRST_DATE,
            reservationTime.getId(),
            theme.getId()
        ));

        List<ReservationRequest> waitlistRequests = List.of(
            new ReservationRequest("네오", FUTURE_FIRST_DATE, reservationTime.getId(), theme.getId()),
            new ReservationRequest("포비", FUTURE_FIRST_DATE, reservationTime.getId(), theme.getId()),
            new ReservationRequest("준", FUTURE_FIRST_DATE, reservationTime.getId(), theme.getId()),
            new ReservationRequest("워니", FUTURE_FIRST_DATE, reservationTime.getId(), theme.getId()),
            new ReservationRequest("브리", FUTURE_FIRST_DATE, reservationTime.getId(), theme.getId())
        );

        ExecutorService executor = Executors.newFixedThreadPool(waitlistRequests.size());

        CountDownLatch readyLatch = new CountDownLatch(waitlistRequests.size());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(waitlistRequests.size());

        Queue<ReservationWithStatus> results = new ConcurrentLinkedQueue<>();
        Queue<Throwable> exceptions = new ConcurrentLinkedQueue<>();

        for (ReservationRequest request : waitlistRequests) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    results.add(reservationService.reserveOrWait(request));
                } catch (Throwable e) {
                    exceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        assertThat(readyLatch.await(3, TimeUnit.SECONDS)).isTrue();
        startLatch.countDown();
        assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();

        executor.shutdown();

        List<Integer> waitingOrders = results.stream()
            .map(ReservationWithStatus::getWaitingOrder)
            .sorted()
            .toList();

        assertThat(exceptions).isEmpty();
        assertThat(results).hasSize(waitlistRequests.size());
        assertThat(results).allSatisfy(result -> assertThat(result.getStatus()).isEqualTo(ReservationStatus.WAITING));
        assertThat(waitingOrders).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void unique_충돌이_발생하면_별도_트랜잭션으로_대기_등록한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        Reservation firstReservation = createReservation("브라운", reservationTime, theme);
        Reservation duplicateReservation = createReservation("브리", reservationTime, theme);

        reservationRepository.save(firstReservation);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(
            status -> reservationRepository.saveAndFlush(duplicateReservation)
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
    void 같은_트랜잭션에서_unique_충돌을_잡으면_JPA_트랜잭션은_롤백된다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();
        Reservation firstReservation = createReservation("브라운", reservationTime, theme);
        Reservation duplicateReservation = createReservation("브리", reservationTime, theme);

        reservationRepository.save(firstReservation);

        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            try {
                reservationRepository.saveAndFlush(duplicateReservation);
            } catch (DataIntegrityViolationException e) {
                return waitlistRepository.save(duplicateReservation, WAITLIST_CREATED_AT);
            }

            throw new AssertionError("같은 슬롯 예약 저장은 unique 충돌이 발생해야 합니다.");
        })).isInstanceOf(UnexpectedRollbackException.class);
    }


    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime).getId();
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme).getId();
        return new Theme(
            id,
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnailImageUrl()
        );
    }

    private Reservation createReservation(String name, ReservationTime reservationTime, Theme theme) {
        Slot slot = slotRepository.getOrCreate(Slot.of(FUTURE_FIRST_DATE, reservationTime, theme));
        Member member = memberRepository.findByName(name)
            .orElseGet(() -> memberRepository.save(new Member(name)));
        return new Reservation(
            member,
            slot
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
