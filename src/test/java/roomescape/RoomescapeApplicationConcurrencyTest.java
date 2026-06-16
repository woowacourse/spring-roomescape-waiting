package roomescape;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.concurrency.Result;
import roomescape.concurrency.RunConcurrency;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReserveResponse;
import roomescape.dto.reservationWaiting.ReservationWaitingRequest;
import roomescape.dto.reservationWaiting.ReservationWaitingResponse;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomescapeApplicationConcurrencyTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @LocalServerPort
    private int port;

    private final LocalDate date = LocalDate.now().plusDays(1);

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("delete from slot");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");

        jdbcTemplate.update("alter table waiting alter column id restart with 1");
        jdbcTemplate.update("alter table reservation alter column id restart with 1");
        jdbcTemplate.update("alter table slot alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");

        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into theme (name, description, url) values ('테스트', '설명', 'url')");
    }

    @Test
    void 같은_슬롯에_동시_예약하면_하나만_성공한다() throws InterruptedException {
        int thread = 10;

        List<ReservationRequest> reservationRequests = new ArrayList<>();

        for (int i = 0; i < thread; i++) {
            reservationRequests.add(new ReservationRequest("test" + i, date, 1L, 1L));
        }

        Result result = RunConcurrency.run(
                reservationRequests.stream()
                        .map(reservationRequest -> (Runnable) () -> reservationService.create(reservationRequest))
                        .toArray(Runnable[]::new)
        );

        assertThat(result.success()).isEqualTo(1);
        assertThat(result.fail()).isEqualTo(thread - 1);
    }

    @Test
    void 같은_사용자가_같은_슬롯에_동시_예약_추가하면_하나만_성공한다() throws InterruptedException {
        int thread = 10;

        ReservationRequest reservationRequest = new ReservationRequest("test", date, 1L, 1L);
        Result result = RunConcurrency.run(thread, () -> reservationService.create(reservationRequest));

        assertThat(result.success()).isEqualTo(1);
        assertThat(result.fail()).isEqualTo(thread - 1);
    }

    @Test
    void 여러_예약이_같은_빈_슬롯으로_예약하면_하나만_성공한다() throws InterruptedException {
        int thread = 10;

        for(int i = 0; i < thread; i++) {
            reservationService.create(new ReservationRequest("test" + i, date.plusDays(i), 1L, 1L));
        }

       List<ReserveResponse> reservations = reservationService.readAll();

        Result result = RunConcurrency.run(
                reservations.stream()
                        .map(reservationResponse -> (Runnable) () -> reservationService.update(
                                reservationResponse.id(),
                                new ReservationRequest(reservationResponse.name(), LocalDate.now().plusDays(thread + 2), 1L, 1L))
                        )
                        .toArray(Runnable[]::new)
        );

        assertThat(result.success()).isEqualTo(1);
        assertThat(result.fail()).isEqualTo(thread - 1);
    }

    @Test
    void 서로_다른_사용자가_같은_슬롯에_동시_대기하면_모두_성공하고_순번이_유일하다() throws InterruptedException {
        int thread = 10;

        ReservationRequest reservationRequest = new ReservationRequest("test", date, 1L, 1L);
        reservationService.create(reservationRequest);

        List<ReservationWaitingRequest> reservationWaitingRequests = new ArrayList<>();
        for (int i = 0; i < thread; i++) {
            reservationWaitingRequests.add(
                    new ReservationWaitingRequest(
                            reservationRequest.name() + i,
                            reservationRequest.date(),
                            reservationRequest.timeId(),
                            reservationRequest.themeId()
                    )
            );
        }

        Result result = RunConcurrency.run(
                reservationWaitingRequests.stream()
                        .map(request -> (Runnable) () -> reservationWaitingService.create(request))
                        .toArray(Runnable[]::new)
        );

        assertThat(result.success()).isEqualTo(thread);
        assertThat(result.fail()).isEqualTo(0);

        List<Long> sequences = reservationWaitingService.readAll().stream()
                .map(ReservationWaitingResponse::sequence)
                .toList();
        Long[] expected = LongStream.rangeClosed(1, thread).boxed().toArray(Long[]::new);

        assertThat(sequences).containsExactlyInAnyOrder(expected);
    }

    @Test
    void 예약_취소와_같은_슬롯_대기등록이_동시에_일어날_때_대기가_있으면_예약도_존재한다() throws InterruptedException {
        ReserveResponse reservation = reservationService.create(new ReservationRequest("test", date, 1L, 1L));

        RunConcurrency.run(
                () -> reservationService.delete(reservation.id()),
                () -> reservationWaitingService.create(new ReservationWaitingRequest("test2", date, 1L, 1L))
        );

        List<ReserveResponse> reservations = reservationService.readAll();
        List<ReservationWaitingResponse> reservationWaitings = reservationWaitingService.readAll();

        assertThat(reservations.size()).isLessThanOrEqualTo(1);
        if (!reservationWaitings.isEmpty()) {
            assertThat(reservations).isNotEmpty();
        }
    }

    @Test
    void 예약_취소와_첫_대기자의_본인_대기취소가_동시에_일어나도_이중_승격되지_않는다() throws InterruptedException {
        ReserveResponse reservation = reservationService.create(new ReservationRequest("test", date, 1L, 1L));
        ReservationWaitingResponse reservationWaiting = reservationWaitingService.create(new ReservationWaitingRequest("test2", date, 1L, 1L));
        reservationWaitingService.create(new ReservationWaitingRequest("test3", date, 1L, 1L));

        RunConcurrency.run(
                () -> reservationService.delete(reservation.id()),
                () -> reservationWaitingService.delete(reservationWaiting.id())
        );

        List<ReserveResponse> reservations = reservationService.readAll();
        List<ReservationWaitingResponse> reservationWaitings = reservationWaitingService.readAll();

        assertThat(reservations.size()).isEqualTo(1);
        assertThat(reservationWaitings).noneMatch(waiting -> waiting.name().equals(reservations.get(0).name()));
    }

    @Test
    void 같은_사용자가_같은_슬롯에_동시_대기하면_하나만_성공한다() throws InterruptedException {
        int thread = 10;
        reservationService.create(new ReservationRequest("test", date, 1L, 1L));

        ReservationWaitingRequest request = new ReservationWaitingRequest("test2", date, 1L, 1L);
        Result result = RunConcurrency.run(thread, () -> reservationWaitingService.create(request));

        assertThat(result.success()).isEqualTo(1);
        assertThat(reservationWaitingService.readByName("test2")).hasSize(1);
    }

    @Test
    void 같은_예약을_동시에_취소해도_대기자는_한_번만_승격된다() throws InterruptedException {
        int thread = 10;
        ReserveResponse reservation = reservationService.create(new ReservationRequest("test", date, 1L, 1L));
        reservationWaitingService.create(new ReservationWaitingRequest("test2", date, 1L, 1L));
        reservationWaitingService.create(new ReservationWaitingRequest("test3", date, 1L, 1L));

        RunConcurrency.run(thread, () -> reservationService.delete(reservation.id()));

        List<ReserveResponse> reservations = reservationService.readAll();
        List<ReservationWaitingResponse> reservationWaitings = reservationWaitingService.readAll();

        assertThat(reservations).hasSize(1);
        assertThat(reservationWaitings).noneMatch(waiting -> waiting.name().equals(reservations.get(0).name()));
    }
}