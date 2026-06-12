package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationSlotRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.service.ReservationService;

@SpringBootTest
public class ReservationConcurrencyTest {

    private static final LocalDateTime REQUEST_TIME = LocalDate.now().atStartOfDay();

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;

    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @Autowired
    ReservationConcurrencyTest(
            ReservationService reservationService,
            ReservationRepository reservationRepository,
            ReservationSlotRepository reservationSlotRepository,
            TimeSlotRepository timeSlotRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.reservationSlotRepository = reservationSlotRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
    }

    @BeforeEach
    void setUp() {
        savedTimeSlot = timeSlotRepository.findByStartAt(LocalTime.of(10, 0))
                .orElseGet(() -> timeSlotRepository.save(new TimeSlot(LocalTime.of(10, 0))));

        savedTheme = themeRepository.save(new Theme("공포", "귀신의 집 탈출", "https://test.com"));
    }

    @Test
    @DisplayName("같은 슬롯에 동시에 예약하면 예약 확정은 하나만 생성된다.")
    void 같은_슬롯_동시_예약_요청() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);

        ReservationSlot slot = reservationSlotRepository.save(
                new ReservationSlot(date, savedTimeSlot, savedTheme)
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<?> first = executorService.submit(() ->
                reservationService.saveReservation("브라운", date, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME)
        );

        Future<?> second = executorService.submit(() ->
                reservationService.saveReservation("네오", date, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME)
        );

        first.get();
        second.get();

        executorService.shutdown();

        List<Reservation> reservations = reservationRepository.findBySlotId(slot.getId());

        assertThat(reservations).hasSize(2);
        assertThat(reservations.stream().filter(Reservation::isReserved)).hasSize(1);
        assertThat(reservations.stream().filter(Reservation::isWaiting)).hasSize(1);
    }

    @Test
    @DisplayName("예약 취소와 같은 슬롯 예약이 동시에 일어나도 예약 확정은 하나만 존재한다.")
    void 예약_취소_추가_동시_요청() throws Exception {
        LocalDate date = LocalDate.now().plusDays(2);

        ReservationSlot slot = reservationSlotRepository.save(
                new ReservationSlot(date, savedTimeSlot, savedTheme)
        );
        Reservation reserved1 = reservationService.saveReservation(
                "브라운", date, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME
        );

        Reservation reserved2 = reservationService.saveReservation(
                "네오", date, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME
        );

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<?> remove = executorService.submit(() ->
                reservationService.removeReservation(reserved1.getId(), "브라운", REQUEST_TIME)
        );

        Future<?> save = executorService.submit(() ->
                reservationService.saveReservation("대길", date, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME)
        );

        remove.get();
        save.get();

        executorService.shutdown();

        List<Reservation> reservations = reservationRepository.findBySlotId(slot.getId());
        Reservation promoted = reservations.stream()
                .filter(reservation -> reservation.getName().equals("네오"))
                .findAny()
                .orElseThrow();

        Reservation waiting = reservations.stream()
                .filter(reservation -> reservation.getName().equals("대길"))
                .findAny()
                .orElseThrow();

        assertThat(reservations).hasSize(2);
        assertThat(reservations.stream().filter(Reservation::isReserved)).hasSize(1);
        assertThat(reservations.stream().filter(Reservation::isWaiting)).hasSize(1);
        assertThat(promoted.isReserved()).isTrue();
        assertThat(waiting.isWaiting()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 같은 슬롯에 대해 동시에 예약하면 예약 확정은 하나만 생성된다.")
    void 존재하지_않는_슬롯에_대한_동시_예약_요청() throws Exception {
        LocalDate date = LocalDate.now().plusDays(300);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<?> first = executorService.submit(() ->
                reservationService.saveReservation("브라운", date, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME)
        );

        Future<?> second = executorService.submit(() ->
                reservationService.saveReservation("네오", date, savedTimeSlot.getId(), savedTheme.getId(), REQUEST_TIME)
        );

        first.get();
        second.get();

        executorService.shutdown();

        long slotId = reservationSlotRepository.findByDateAndTimeIdAndThemeId(
                date,
                savedTimeSlot.getId(),
                savedTheme.getId())
                .orElseThrow().getId();

        List<Reservation> reservations = reservationRepository.findBySlotId(slotId);

        assertThat(reservations).hasSize(2);
        assertThat(reservations.stream().filter(Reservation::isReserved)).hasSize(1);
        assertThat(reservations.stream().filter(Reservation::isWaiting)).hasSize(1);
    }
}
