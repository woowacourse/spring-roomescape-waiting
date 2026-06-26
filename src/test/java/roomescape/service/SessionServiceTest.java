package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.*;
import roomescape.exception.*;
import roomescape.repository.*;
import roomescape.service.dto.Booking;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionServiceTest {

    private final LocalDate futureDate = LocalDate.now().plusDays(1);
    private SessionService sessionService;
    private FakeReservationRepository reservationRepository;
    private FakeWaitingRepository waitingRepository;
    private FakeSessionRepository sessionRepository;
    private FakeTimeSlotRepository timeSlotRepository;
    private FakeThemeRepository themeRepository;

    private TimeSlot savedTimeSlot;
    private Theme savedTheme;
    private Session savedSession;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        waitingRepository = new FakeWaitingRepository();
        sessionRepository = new FakeSessionRepository();
        timeSlotRepository = new FakeTimeSlotRepository();
        themeRepository = new FakeThemeRepository();

        ReservationService reservationService = new ReservationService(reservationRepository);
        WaitingService waitingService = new WaitingService(waitingRepository);
        TimeSlotService timeSlotService = new TimeSlotService(timeSlotRepository);
        ThemeService themeService = new ThemeService(themeRepository);
        sessionService = new SessionService(sessionRepository, timeSlotService, themeService,
                reservationService, waitingService);

        savedTimeSlot = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(Theme.transientOf("이름", "설명", "test.com"));
        savedSession = sessionRepository.save(Session.transientOf(futureDate, savedTimeSlot, savedTheme));
    }

    @Test
    @DisplayName("날짜, 시간, 테마로 새로운 세션을 생성한다.")
    void createSession() {
        LocalDate newDate = futureDate.plusDays(1);
        Session session = sessionService.createSession(newDate, savedTimeSlot.getId(), savedTheme.getId());
        assertThat(session.getDate()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("이미 존재하는 세션을 중복 생성하려 하면 예외가 발생한다.")
    void createDuplicateSession() {
        assertThatThrownBy(() -> sessionService.createSession(futureDate, savedTimeSlot.getId(), savedTheme.getId()))
                .isInstanceOf(DuplicateSessionException.class);
    }

    @Test
    @DisplayName("날짜에 해당하는 모든 시간/테마 조합으로 세션을 일괄 생성한다.")
    void createSessionsForDate() {
        LocalDate newDate = futureDate.plusDays(7);
        List<Session> sessions = sessionService.createSessionsForDate(newDate);
        assertThat(sessions).hasSize(1);
    }

    @Test
    @DisplayName("존재하는 세션에 예약을 생성한다.")
    void makeReservation() {
        Reservation reservation = sessionService.makeReservation(basicRequest("브라운"));
        assertThat(reservation.getSession().getTimeSlot().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하지 않는 세션에 예약을 시도하면 예외가 발생한다.")
    void makeReservation_SessionNotFound() {
        assertThatThrownBy(() -> sessionService.makeReservation(
                new ReservationRequest("브라운", futureDate, 999L, savedTheme.getId())))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    @DisplayName("중복된 예약을 생성하려 하면 예외가 발생한다.")
    void makeReservationDuplicate() {
        sessionService.makeReservation(basicRequest("브라운"));
        assertThatThrownBy(() -> sessionService.makeReservation(basicRequest("토미")))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("지나간 날짜의 세션에 예약을 생성하면 예외가 발생한다.")
    void makeReservation_PastDate() {
        Session pastSession = sessionRepository.save(
                Session.transientOf(LocalDate.now().minusDays(1), savedTimeSlot, savedTheme));
        assertThatThrownBy(() -> sessionService.makeReservation(
                new ReservationRequest("브라운", pastSession.getDate(), savedTimeSlot.getId(), savedTheme.getId())))
                .isInstanceOf(PastTimeException.class);
    }

    @Test
    @DisplayName("예약을 삭제하면 목록에서 사라진다.")
    void cancelReservation() {
        Reservation reservation = sessionService.makeReservation(basicRequest("브라운"));
        sessionService.cancelReservation(reservation.getId(), "브라운");
        assertThat(sessionService.allReservations()).isEmpty();
    }

    @Test
    @DisplayName("예약 삭제 후 세션은 삭제되지 않고 유지된다.")
    void cancelReservation_SessionRemains() {
        Reservation reservation = sessionService.makeReservation(basicRequest("브라운"));
        sessionService.cancelReservation(reservation.getId(), "브라운");
        assertThat(sessionRepository.findAll()).isNotEmpty();
    }

    @Test
    @DisplayName("예약 삭제 시 대기자가 있다면 최우선 대기자가 예약으로 승급된다.")
    void cancelReservation_PromoteWaiting() {
        Reservation reservation = sessionService.makeReservation(basicRequest("브라운"));
        waitingRepository.save(Waiting.transientOf("네오", reservation.getSession()));
        sessionService.cancelReservation(reservation.getId(), "브라운");
        List<Reservation> reservations = sessionService.allReservations();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getName()).isEqualTo("네오");
    }

    @Test
    @DisplayName("이미 지난 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void cancelReservation_Past() {
        Reservation pastReservation = savePastReservation();
        assertThatThrownBy(() -> sessionService.cancelReservation(pastReservation.getId(), "브라운"))
                .isInstanceOf(PastSessionControlException.class);
    }

    @Test
    @DisplayName("다른 사용자의 예약을 삭제하려고 시도하면 예외가 발생한다.")
    void cancelReservation_WrongOwner() {
        Reservation reservation = sessionService.makeReservation(basicRequest("브라운"));
        assertThatThrownBy(() -> sessionService.cancelReservation(reservation.getId(), "네오"))
                .isInstanceOf(InvalidOwnershipException.class);
    }

    @Test
    @DisplayName("예약의 전체 정보를 수정(PUT)하고 반환한다.")
    void rescheduleReservation() {
        Reservation reservation = sessionService.makeReservation(basicRequest("브라운"));
        TimeSlot newTime = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(14, 0)));
        Session newSession = sessionRepository.save(Session.transientOf(futureDate, newTime, savedTheme));
        Reservation updated = sessionService.rescheduleReservation(
                reservation.getId(), "브라운",
                new ReservationRequest("브라운", futureDate, newTime.getId(), savedTheme.getId()));
        assertThat(updated.getSession().getId()).isEqualTo(newSession.getId());
    }

    @Test
    @DisplayName("예약 변경 시 기존 세션에 대기자가 있으면 최우선 대기자가 승급된다.")
    void rescheduleReservation_PromoteWaiting() {
        Reservation reservation = sessionService.makeReservation(basicRequest("브라운"));
        waitingRepository.save(Waiting.transientOf("네오", reservation.getSession()));
        TimeSlot newTime = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(15, 0)));
        sessionRepository.save(Session.transientOf(futureDate, newTime, savedTheme));
        sessionService.rescheduleReservation(reservation.getId(), "브라운",
                new ReservationRequest("브라운", futureDate, newTime.getId(), savedTheme.getId()));
        assertThat(sessionService.allReservations()).extracting(Reservation::getName)
                .containsExactlyInAnyOrder("브라운", "네오");
    }

    @Test
    @DisplayName("예약의 일부 정보를 수정(PATCH)하고 반환한다.")
    void patchReservation() {
        Reservation reservation = sessionService.makeReservation(basicRequest("브라운"));
        Reservation updated = sessionService.patchReservation(
                reservation.getId(), "브라운",
                new ReservationPatchRequest("브라운", null, null, null));
        assertThat(updated.getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("사용자 이름으로 예약과 대기 목록을 통합하여 조회한다.")
    void findReservationByName() {
        sessionService.makeReservation(basicRequest("브라운"));
        List<Booking> bookings = sessionService.findReservationByName("브라운");
        assertThat(bookings).hasSize(1);
        assertThat(bookings.getFirst().isReserved()).isTrue();
    }

    @Test
    @DisplayName("예약이 있는 세션에 대기를 추가한다.")
    void addWaiting() {
        sessionService.makeReservation(basicRequest("브라운"));
        Waiting waiting = sessionService.addWaiting(
                new WaitingRequest("네오", futureDate, savedTimeSlot.getId(), savedTheme.getId()));
        assertThat(waiting.getName()).isEqualTo("네오");
    }

    @Test
    @DisplayName("예약 대기를 취소한다.")
    void cancelWaiting() {
        sessionService.makeReservation(basicRequest("브라운"));
        Waiting waiting = sessionService.addWaiting(
                new WaitingRequest("네오", futureDate, savedTimeSlot.getId(), savedTheme.getId()));
        sessionService.cancelWaiting(waiting.getId(), "네오");
        assertThat(waitingRepository.existsBySession(savedSession)).isFalse();
    }

    @Test
    @DisplayName("테마와 날짜를 기준으로 이용 가능한 예약 시간 목록을 조회한다.")
    void findAvailableTimes() {
        List<roomescape.service.dto.AvailableTimeSlot> availableTimes =
                sessionService.findAvailableTimes(savedTheme.getId(), futureDate);
        assertThat(availableTimes).isNotNull();
    }

    private ReservationRequest basicRequest(String name) {
        return new ReservationRequest(name, futureDate, savedTimeSlot.getId(), savedTheme.getId());
    }

    private Reservation savePastReservation() {
        Session pastSession = sessionRepository.save(
                Session.transientOf(LocalDate.now().minusDays(1), savedTimeSlot, savedTheme));
        return reservationRepository.save(Reservation.transientOf("브라운", pastSession));
    }
}
