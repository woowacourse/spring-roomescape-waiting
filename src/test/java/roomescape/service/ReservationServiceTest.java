package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.PastTimeException;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.FakeTimeSlotRepository;
import roomescape.repository.WaitingRepository;

class ReservationServiceTest {

    private ReservationService reservationService;
    private FakeReservationRepository reservationRepository;
    private FakeTimeSlotRepository timeSlotRepository;
    private FakeThemeRepository themeRepository;

    @Mock
    private WaitingRepository waitingRepository;

    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        timeSlotRepository = new FakeTimeSlotRepository();
        reservationRepository = new FakeReservationRepository();
        themeRepository = new FakeThemeRepository();
        waitingRepository = Mockito.mock(WaitingRepository.class);

        reservationService = new ReservationService(reservationRepository, timeSlotRepository, themeRepository,
                waitingRepository);

        savedTimeSlot = timeSlotRepository.save(new TimeSlot(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(new Theme("이름", "설명", "test.com"));
    }

    @Test
    @DisplayName("원시값을 받아 연관된 객체를 조회하여 조립한 뒤 예약을 생성한다.")
    void 예약_저장() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation reservation = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId());
        assertThat(reservation.getTimeSlot().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("중복된 예약을 생성하려 하면 예외가 발생한다.")
    void 중복_예약_예외_발생() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId());

        assertThatThrownBy(
                () -> reservationService.saveReservation("토미", futureDate, savedTimeSlot.getId(), savedTheme.getId()))
                .isInstanceOf(DuplicateReservationException.class)
                .hasMessage("이미 예약된 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
    }

    @Test
    @DisplayName("존재하는 예약을 식별자를 통해 삭제하면 목록에서 사라진다.")
    void 예약_삭제() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation reservation = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId());
        reservationService.removeReservation(reservation.getId(), "브라운");
        assertThat(reservationService.findAllReservations()).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회하여 반환한다.")
    void 전체_예약_조회() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId());
        List<Reservation> reservations = reservationService.findAllReservations();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 객체를 조회한다.")
    void 식별자로_예약_조회() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId());
        Reservation foundReservation = reservationService.findReservationById(savedReservation.getId());
        assertThat(foundReservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("지나간 날짜에 대한 예약 생성은 불가능하다.")
    void 지난_날짜_예약_생성_예외_발생() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        assertThatThrownBy(
                () -> reservationService.saveReservation("브라운", pastDate, savedTimeSlot.getId(), savedTheme.getId()))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("지난 날짜/시간으로 예약하실 수 없습니다.");
    }

    @Test
    @DisplayName("지나간 시간에 대한 예약 생성은 불가능하다.")
    void 지난_시간_예약_생성_예외_발생() {
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.of(0, 0);
        TimeSlot pastTimeSlot = timeSlotRepository.save(new TimeSlot(pastTime));

        assertThatThrownBy(
                () -> reservationService.saveReservation("브라운", today, pastTimeSlot.getId(), savedTheme.getId()))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("지난 날짜/시간으로 예약하실 수 없습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void 지난_예약_삭제_예외_발생() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = reservationRepository.save(
                new Reservation(null, "브라운", pastDate, savedTimeSlot, savedTheme)
        );

        assertThatThrownBy(() -> reservationService.removeReservation(pastReservation.getId(), "브라운"))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("이미 지난 예약은 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약을 수정하려고 시도하면 예외가 발생한다.")
    void 지난_예약_수정_예외_발생() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = reservationRepository.save(
                new Reservation(null, "브라운", pastDate, savedTimeSlot, savedTheme)
        );

        assertThatThrownBy(() -> reservationService.updateReservation(
                pastReservation.getId(), "브라운", LocalDate.now().plusDays(1), savedTimeSlot.getId()
        )).isInstanceOf(PastTimeException.class)
                .hasMessage("이미 지난 예약은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("다른 사용자의 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void 다른_사용자_예약_삭제_예외_발생() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation savedReservation = reservationRepository.save(
                new Reservation(null, "브라운", futureDate, savedTimeSlot, savedTheme)
        );

        assertThatThrownBy(() -> reservationService.removeReservation(savedReservation.getId(), "네오"))
                .isInstanceOf(InvalidOwnershipException.class)
                .hasMessage("본인의 예약만 제어할 수 있습니다.");
    }

    @Test
    @DisplayName("예약을 이미 예약된 다른 시간으로 수정하려 하면 예외가 발생한다.")
    void 중복_시간_예약_수정_예외_발생() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation target = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId());
        TimeSlot otherTime = timeSlotRepository.save(new TimeSlot(LocalTime.of(13, 0)));
        reservationService.saveReservation("네오", futureDate, otherTime.getId(), savedTheme.getId());

        assertThatThrownBy(() -> reservationService.updateReservation(
                target.getId(), "브라운", futureDate, otherTime.getId()
        )).isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("자기 자신의 예약 날짜와 시간을 그대로 유지하면 수정하지 않는다.")
    void 같은_날짜_시간_예약_수정_검증() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        Reservation target = reservationService.saveReservation("브라운", futureDate, savedTimeSlot.getId(),
                savedTheme.getId());

        reservationService.updateReservation(target.getId(), "브라운", futureDate, savedTimeSlot.getId());

        Reservation reservation = reservationService.findReservationById(target.getId());
        assertThat(reservation.getName()).isEqualTo("브라운");
        assertThat(reservation.getDate()).isEqualTo(futureDate);
        assertThat(reservation.getTimeSlot()).isEqualTo(savedTimeSlot);
    }
}
