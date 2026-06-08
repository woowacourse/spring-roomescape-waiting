package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.repository.FakeReservationRepository;

class ReservationServiceTest {

    private final LocalDate futureDate = LocalDate.now().plusDays(1);
    private ReservationService reservationService;
    private FakeReservationRepository reservationRepository;
    private Session session;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationService = new ReservationService(reservationRepository);

        TimeSlot timeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "test.com");
        session = new Session(1L, futureDate, timeSlot, theme);
    }

    @Test
    @DisplayName("원시값을 받아 예약을 생성하고 저장한다.")
    void save() {
        Reservation reservation = reservationService.save("브라운", session);
        assertThat(reservation.getSession().getTimeSlot().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("모든 예약 목록을 조회하여 반환한다.")
    void findAll() {
        reservationService.save("브라운", session);
        List<Reservation> reservations = reservationService.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 객체를 조회한다.")
    void findById() {
        Reservation saved = reservationService.save("브라운", session);
        Reservation found = reservationService.findById(saved.getId());
        assertThat(found.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("이름으로 해당 사용자의 예약 목록을 조회한다.")
    void findByName() {
        reservationService.save("브라운", session);
        List<Reservation> reservations = reservationService.findByName("브라운");
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("세션으로 해당 예약을 조회한다.")
    void findBySession() {
        reservationService.save("브라운", session);
        Optional<Reservation> found = reservationService.findBySession(session);
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("예약을 삭제하면 목록에서 사라진다.")
    void delete() {
        Reservation saved = reservationService.save("브라운", session);
        reservationService.delete(saved.getId());
        assertThat(reservationService.findAll()).isEmpty();
    }
}
