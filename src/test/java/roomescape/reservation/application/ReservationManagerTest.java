package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.application.dto.ReservationCancelCommand;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.domain.Status;
import roomescape.theme.application.ThemeService;
import roomescape.theme.application.dto.ThemeCommand;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.domain.Theme;
import roomescape.time.application.ReservationTimeService;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.domain.ReservationTime;

@SpringBootTest(properties = {"spring.datasource.hikari.maximum-pool-size=30"})
class ReservationManagerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    @Autowired
    private ReservationManager manager;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ThemeService themeService;

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        ReservationTimeInfo reservationTimeInfo = reservationTimeService.addReservationTime(
                ReservationTimeCommand.builder()
                        .startAt(LocalTime.now(clock))
                        .build()
        );

        time = ReservationTime.builder()
                .id(reservationTimeInfo.id())
                .startAt(reservationTimeInfo.startAt())
                .build();

        ThemeInfo themeInfo = themeService.addTheme(ThemeCommand.builder()
                .name("추리")
                .description("추리하기")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .build()
        );

        theme = Theme.builder()
                .id(themeInfo.id())
                .name(themeInfo.name())
                .description(themeInfo.description())
                .durationTime(themeInfo.durationTime())
                .thumbnailImageUrl(themeInfo.thumbnailImageUrl())
                .build();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation");
        jdbcTemplate.execute("TRUNCATE TABLE time_slot");
        jdbcTemplate.execute("TRUNCATE TABLE theme");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    @DisplayName("예약이 없으면 확정 예약으로 등록된다.")
    void addActiveReservationTest() {
        ReservationInfo reservationInfo = manager.addReservation(createCommand("포비", time.getId()));
        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("예약이 있으면 대기 예약으로 등록된다.")
    void addPendingReservationTest() {
        manager.addReservation(createCommand("포비", time.getId()));
        ReservationInfo reservationInfo = manager.addReservation(createCommand("리사", time.getId()));
        Assertions.assertThat(reservationInfo.status()).isEqualTo(Status.PENDING);
    }

    @Test
    @DisplayName("대기 중인 예약을 빈 시간대로 변경하면 확정 예약으로 승격된다.")
    void changePendingToEmptySlotTest() {
        manager.addReservation(createCommand("포비", time.getId()));
        ReservationInfo lisaInfo = manager.addReservation(createCommand("리사", time.getId()));

        ReservationTime newTime = createNewTime();

        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("리사")
                .date(LocalDate.now(clock))
                .timeId(newTime.getId())
                .themeId(theme.getId())
                .build();
        ReservationInfo changedInfo = manager.changeReservation(lisaInfo.id(), changeCommand);

        Assertions.assertThat(changedInfo.status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("대기 중인 예약을 꽉 찬 시간대로 변경하면 여전히 대기 예약으로 남는다.")
    void changePendingToFullSlotTest() {
        manager.addReservation(createCommand("포비", time.getId()));
        ReservationInfo lisaInfo = manager.addReservation(createCommand("리사", time.getId()));

        ReservationTime newTime = createNewTime();
        manager.addReservation(createCommand("브라운", newTime.getId()));

        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("리사")
                .date(LocalDate.now(clock))
                .timeId(newTime.getId())
                .themeId(theme.getId())
                .build();
        ReservationInfo changedInfo = manager.changeReservation(lisaInfo.id(), changeCommand);

        Assertions.assertThat(changedInfo.status()).isEqualTo(Status.PENDING);
    }

    @Test
    @DisplayName("확정 예약을 다른 빈 시간대로 변경하면, 원래 자리의 대기자가 확정으로 승격된다.")
    void changeActiveAndPromotePendingTest() {
        ReservationInfo pobiInfo = manager.addReservation(createCommand("포비", time.getId()));
        manager.addReservation(createCommand("리사", time.getId()));

        ReservationTime newTime = createNewTime();

        ReservationChangeCommand changeCommand = ReservationChangeCommand.builder()
                .name("포비")
                .date(LocalDate.now(clock))
                .timeId(newTime.getId())
                .themeId(theme.getId())
                .build();
        ReservationInfo changedInfo = manager.changeReservation(pobiInfo.id(), changeCommand);

        Assertions.assertThat(changedInfo.status()).isEqualTo(Status.ACTIVE);

        List<ReservationPendingInfo> lisaReservations = manager.getReservationsByName("리사");
        Assertions.assertThat(lisaReservations.get(0).status()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("확정 예약을 취소하면 대기자가 확정 예약으로 승격된다.")
    void cancelActiveAndPromotePendingTest() {
        ReservationInfo pobiInfo = manager.addReservation(createCommand("포비", time.getId()));
        ReservationInfo lisaReservationInfo = manager.addReservation(createCommand("리사", time.getId()));

        ReservationCancelCommand cancelCommand = ReservationCancelCommand.builder()
                .name("포비")
                .build();
        manager.cancelReservation(pobiInfo.id(), cancelCommand);

        List<ReservationPendingInfo> lisaReservations = manager.getReservationsByName("리사");
        Assertions.assertThat(lisaReservations.get(0).status()).isEqualTo(Status.ACTIVE);
        Assertions.assertThat(lisaReservations.get(0).id()).isEqualTo(lisaReservationInfo.id());
    }

    @Test
    @DisplayName("20명의 사용자가 동시에 같은 예약을 시도하면 1명만 확정(ACTIVE)되고 19명은 대기(PENDING) 상태가 된다.")
    void concurrentAddReservationTest() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String username = "사용자" + i;
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    ReservationCreateCommand command = createCommand(username, time.getId());
                    manager.addReservation(command);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.out.println(username + " 예약 실패: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        List<ReservationInfo> allReservations = manager.getReservations();

        long activeCount = allReservations.stream()
                .filter(res -> res.status() == Status.ACTIVE)
                .count();
        long pendingCount = allReservations.stream()
                .filter(res -> res.status() == Status.PENDING)
                .count();

        Assertions.assertThat(allReservations).hasSize(threadCount);
        Assertions.assertThat(activeCount).isEqualTo(1);
        Assertions.assertThat(pendingCount).isEqualTo(threadCount - 1);
    }

    private ReservationCreateCommand createCommand(String name, Long timeId) {
        return ReservationCreateCommand.builder()
                .name(name)
                .date(LocalDate.now(clock))
                .timeId(timeId)
                .themeId(theme.getId())
                .build();
    }

    private ReservationTime createNewTime() {
        ReservationTimeInfo info = reservationTimeService.addReservationTime(
                ReservationTimeCommand.builder()
                        .startAt(LocalTime.now(clock).plusHours(1))
                        .build()
        );
        return ReservationTime.builder()
                .id(info.id())
                .startAt(info.startAt())
                .build();
    }
}
