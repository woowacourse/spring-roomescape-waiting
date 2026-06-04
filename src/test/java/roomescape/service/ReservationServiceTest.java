package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.PastReservationControlException;
import roomescape.exception.PastTimeException;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeSlotRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.FakeTimeSlotRepository;
import roomescape.repository.FakeWaitingRepository;

class ReservationServiceTest {

    private final LocalDate futureDate = LocalDate.now().plusDays(1);
    private ReservationService reservationService;
    private FakeReservationRepository reservationRepository;
    private FakeTimeSlotRepository timeSlotRepository;
    private FakeThemeRepository themeRepository;
    private FakeWaitingRepository fakeWaitingRepository;
    private FakeSlotRepository fakeSlotRepository;
    private SlotService slotService;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;
    private ReservationRequest basicReservationRequest;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        timeSlotRepository = new FakeTimeSlotRepository();
        themeRepository = new FakeThemeRepository();
        fakeWaitingRepository = new FakeWaitingRepository();
        fakeSlotRepository = new FakeSlotRepository();
        slotService = new SlotService(fakeSlotRepository, timeSlotRepository, themeRepository);
        reservationService = new ReservationService(reservationRepository, fakeWaitingRepository, slotService);

        savedTimeSlot = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(Theme.transientOf("이름", "설명", "test.com"));
        basicReservationRequest = new ReservationRequest("브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId());
    }

    @Test
    @DisplayName("원시값을 받아 연관된 객체를 조회하여 조립한 뒤 예약을 생성한다.")
    void saveReservation() {
        Reservation reservation = reservationService.saveReservation(basicReservationRequest);
        assertThat(reservation.getSlot().getTimeSlot().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("중복된 예약을 생성하려 하면 예외가 발생한다.")
    void saveReservationDuplicate() {
        reservationService.saveReservation(basicReservationRequest);
        assertThatThrownBy(
                () -> reservationService.saveReservation(
                        new ReservationRequest("토미", futureDate, savedTimeSlot.getId(), savedTheme.getId())))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("존재하는 예약을 식별자를 통해 삭제하면 목록에서 사라진다.")
    void removeReservation() {
        Reservation reservation = reservationService.saveReservation(basicReservationRequest);
        reservationService.removeReservation(reservation.getId(), "브라운");
        assertThat(reservationService.allReservations()).isEmpty();
    }

    @Test
    @DisplayName("모든 예약 목록을 조회하여 반환한다.")
    void allReservations() {
        reservationService.saveReservation(basicReservationRequest);
        List<Reservation> reservations = reservationService.allReservations();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("식별자를 통해 특정 예약 객체를 조회한다.")
    void findReservation() {
        Reservation savedReservation = reservationService.saveReservation(basicReservationRequest);
        Reservation foundReservation = reservationService.findReservationById(savedReservation.getId());
        assertThat(foundReservation.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("지나간 날짜에 대한 예약 생성은 불가능하다.")
    void saveReservation_PastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertThatThrownBy(
                () -> reservationService.saveReservation(
                        new ReservationRequest("브라운", pastDate, savedTimeSlot.getId(), savedTheme.getId())))
                .isInstanceOf(PastTimeException.class);
    }

    @Test
    @DisplayName("지나간 시간에 대한 예약 생성은 불가능하다.")
    void saveReservation_PastTime() {
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusHours(1);
        TimeSlot pastTimeSlot = timeSlotRepository.save(TimeSlot.transientOf(pastTime));
        assertThatThrownBy(
                () -> reservationService.saveReservation(
                        new ReservationRequest("브라운", today, pastTimeSlot.getId(), savedTheme.getId())))
                .isInstanceOf(PastTimeException.class);
    }

    @Test
    @DisplayName("이미 지난 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void removeReservation_Past() {
        Reservation pastReservation = savePastReservation(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> reservationService.removeReservation(pastReservation.getId(), "브라운"))
                .isInstanceOf(PastReservationControlException.class);
    }

    @Test
    @DisplayName("이미 지난 예약을 전체 수정(PUT)하려고 시도하면 예외가 발생한다.")
    void putReservation_Past() {
        Reservation pastReservation = savePastReservation(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> reservationService.putReservation(
                pastReservation.getId(), "브라운", new ReservationRequest(
                        "브라운", futureDate, savedTimeSlot.getId(), savedTheme.getId())
        )).isInstanceOf(PastReservationControlException.class);
    }

    @Test
    @DisplayName("이미 지난 예약을 부분 수정(PATCH)하려고 시도하면 예외가 발생한다.")
    void reschedule_Past() {
        Reservation pastReservation = savePastReservation(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> reservationService.patchReservation(
                pastReservation.getId(), "브라운", new ReservationPatchRequest("브라운", null, null, null)))
                .isInstanceOf(PastReservationControlException.class);
    }

    @Test
    @DisplayName("다른 사용자의 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void removeReservation_WrongOwner() {
        Reservation savedReservation = reservationService.saveReservation(basicReservationRequest);
        assertThatThrownBy(() -> reservationService.removeReservation(savedReservation.getId(), "네오"))
                .isInstanceOf(InvalidOwnershipException.class);
    }

    @Test
    @DisplayName("예약을 이미 예약된 다른 시간으로 수정(PUT)하려 하면 예외가 발생한다.")
    void putReservation_Duplicated() {
        Reservation target = reservationService.saveReservation(basicReservationRequest);
        TimeSlot otherTime = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(13, 0)));
        reservationService.saveReservation(
                new ReservationRequest("네오", futureDate, otherTime.getId(), savedTheme.getId()));
        assertThatThrownBy(() -> reservationService.putReservation(
                target.getId(), "브라운", new ReservationRequest("브라운", futureDate, otherTime.getId(), savedTheme.getId())
        )).isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("대기가 없는 슬롯은 예약 삭제와 함께 삭제된다.")
    void removeReservation_SlotRemove() {
        Reservation reservation = reservationService.saveReservation(basicReservationRequest);
        reservationService.removeReservation(reservation.getId(), reservation.getName());
        assertThat(fakeSlotRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("예약 삭제 시 대기자가 있다면 최우선 대기자가 예약으로 승급된다.")
    void removeReservation_PromoteWaiting() {
        Reservation reservation = reservationService.saveReservation(basicReservationRequest);
        fakeWaitingRepository.save(Waiting.transientOf("네오", reservation.getSlot()));
        reservationService.removeReservation(reservation.getId(), "브라운");
        List<Reservation> reservations = reservationService.allReservations();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getName()).isEqualTo("네오");
        assertThat(fakeWaitingRepository.isExistsBySlotId(reservation.getSlot().getId())).isFalse();
    }

    @Test
    @DisplayName("예약 변경으로 슬롯이 달라지면 기존 슬롯의 최우선 대기자가 예약으로 승급된다.")
    void putReservation_PromoteWaiting() {
        Reservation reservation = reservationService.saveReservation(basicReservationRequest);
        fakeWaitingRepository.save(Waiting.transientOf("네오", reservation.getSlot()));
        TimeSlot newTime = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(15, 0)));
        ReservationRequest putRequest = new ReservationRequest("브라운", futureDate, newTime.getId(), savedTheme.getId());
        reservationService.putReservation(reservation.getId(), "브라운", putRequest);
        List<Reservation> reservations = reservationService.allReservations();
        assertThat(reservations).hasSize(2);
        assertThat(reservations).extracting(Reservation::getName).containsExactlyInAnyOrder("브라운", "네오");
    }

    private Reservation savePastReservation(LocalDate pastDate) {
        Slot slot = slotService.resolveNewSlot(pastDate, savedTimeSlot.getId(), savedTheme.getId());
        return reservationRepository.save(Reservation.transientOf("브라운", slot));
    }
}
