package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.PastReservationControlException;
import roomescape.exception.PastTimeException;
import roomescape.repository.FakeReservationRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.FakeTimeSlotRepository;
import roomescape.repository.FakeWaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest {

    private final LocalDate futureDate = LocalDate.now().plusDays(1);
    private ReservationService reservationService;
    private FakeReservationRepository reservationRepository;
    private FakeTimeSlotRepository timeSlotRepository;
    private FakeThemeRepository themeRepository;
    private FakeWaitingRepository fakeWaitingRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;
    private ReservationRequest basicReservationRequest;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        timeSlotRepository = new FakeTimeSlotRepository();
        themeRepository = new FakeThemeRepository();
        fakeWaitingRepository = new FakeWaitingRepository();
        reservationService = new ReservationService(reservationRepository, timeSlotRepository, themeRepository, fakeWaitingRepository);

        savedTimeSlot = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(Theme.transientOf("이름", "설명", "test.com"));
        basicReservationRequest = new ReservationRequest("브라운", LocalDate.now().plusDays(1), savedTimeSlot.getId(), savedTheme.getId());
    }

    @Test
    @DisplayName("원시값을 받아 연관된 객체를 조회하여 조립한 뒤 예약을 생성한다.")
    void saveReservation() {
        Reservation reservation = reservationService.saveReservation(basicReservationRequest);
        assertThat(reservation.getTimeSlot().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("중복된 예약을 생성하려 하면 예외가 발생한다.")
    void saveReservationDuplicate() {
        reservationService.saveReservation(basicReservationRequest);
        assertThatThrownBy(
                () -> reservationService.saveReservation(new ReservationRequest("토미", futureDate, savedTimeSlot.getId(), savedTheme.getId())))
                .isInstanceOf(DuplicateReservationException.class)
                .hasMessageContaining("는 이미 예약되어 있습니다.");
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
                () -> reservationService.saveReservation(new ReservationRequest("브라운", pastDate, savedTimeSlot.getId(), savedTheme.getId())))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("지난 날짜로 예약하실 수 없습니다.");
    }

    @Test
    @DisplayName("지나간 시간에 대한 예약 생성은 불가능하다.")
    void saveReservation_PastTime() {
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusHours(1);
        TimeSlot pastTimeSlot = timeSlotRepository.save(TimeSlot.transientOf(pastTime));
        assertThatThrownBy(
                () -> reservationService.saveReservation(new ReservationRequest("브라운", today, pastTimeSlot.getId(), savedTheme.getId())))
                .isInstanceOf(PastTimeException.class)
                .hasMessage("지난 시간으로 예약하실 수 없습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void removeReservation_Past() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = reservationRepository.save(
                Reservation.transientOf("브라운", pastDate, savedTimeSlot, savedTheme)
        );

        assertThatThrownBy(() -> reservationService.removeReservation(pastReservation.getId(), "브라운"))
                .isInstanceOf(PastReservationControlException.class)
                .hasMessage("이미 지난 예약은 수정/삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약을 전체 수정(PUT)하려고 시도하면 예외가 발생한다.")
    void putReservation_Past() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = reservationRepository.save(
                Reservation.transientOf("브라운", pastDate, savedTimeSlot, savedTheme)
        );

        assertThatThrownBy(() -> reservationService.putReservation(
                pastReservation.getId(), "브라운", new ReservationRequest(
                        "브라운", LocalDate.now().plusDays(1), savedTimeSlot.getId(), savedTheme.getId())
        )).isInstanceOf(PastReservationControlException.class)
                .hasMessage("이미 지난 예약은 수정/삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 지난 예약을 부분 수정(PATCH)하려고 시도하면 예외가 발생한다.")
    void reschedule_Past() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Reservation pastReservation = reservationRepository.save(
                Reservation.transientOf("브라운", pastDate, savedTimeSlot, savedTheme)
        );
        assertThatThrownBy(() -> reservationService.patchReservation(
                pastReservation.getId(), "브라운", new ReservationPatchRequest("브라운", null, null, null)))
                .isInstanceOf(PastReservationControlException.class)
                .hasMessage("이미 지난 예약은 수정/삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("다른 사용자의 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void removeReservation_WrongOwner() {
        Reservation savedReservation = reservationRepository.save(
                Reservation.transientOf("브라운", futureDate, savedTimeSlot, savedTheme)
        );
        assertThatThrownBy(() -> reservationService.removeReservation(savedReservation.getId(), "네오"))
                .isInstanceOf(InvalidOwnershipException.class)
                .hasMessage("본인의 예약만 제어할 수 있습니다.");
    }

    @Test
    @DisplayName("예약을 이미 예약된 다른 시간으로 수정(PUT)하려 하면 예외가 발생한다.")
    void putReservation_Duplicated() {
        Reservation target = reservationService.saveReservation(new ReservationRequest("브라운", futureDate,
                savedTimeSlot.getId(), savedTheme.getId()));
        TimeSlot otherTime = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(13, 0)));
        reservationService.saveReservation(new ReservationRequest("네오", futureDate, otherTime.getId(), savedTheme.getId()));
        assertThatThrownBy(() -> reservationService.putReservation(
                target.getId(), "브라운", new ReservationRequest("브라운", futureDate, otherTime.getId(), savedTheme.getId())
        )).isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("자기 자신의 예약 시간을 그대로 유지한 채 이름만 수정(PUT)한다.")
    void putReservation_SelfDuplicate_Bug() {
        Reservation target = reservationService.saveReservation(new ReservationRequest("브라운", futureDate,
                savedTimeSlot.getId(), savedTheme.getId()));

        reservationService.putReservation(
                target.getId(), "브라운", new ReservationRequest("새로운이름", futureDate, savedTimeSlot.getId(), savedTheme.getId())
        );

        assertThat(reservationService.findReservationById(target.getId()).getName()).isEqualTo("새로운이름");
    }
}
