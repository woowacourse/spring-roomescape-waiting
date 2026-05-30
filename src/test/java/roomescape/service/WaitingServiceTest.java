package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.PastTimeException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.repository.WaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private ThemeRepository themeRepository;

    private WaitingService waitingService;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        savedTimeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        savedTheme = new Theme(1L, "이름", "설명", "test.com");
        waitingService = new WaitingService(waitingRepository, reservationRepository,
                timeSlotRepository, themeRepository);
    }

    @Test
    @DisplayName("존재하는 예약 대기를 삭제한다.")
    void deleteWaiting() {
        Waiting waiting = createWaitingEntity();
        given(waitingRepository.findById(1L)).willReturn(Optional.of(waiting));

        assertThatCode(() -> waitingService.removeWaiting(1L, "브라운")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 예약 대기를 삭제하면, 예외가 발생한다.")
    void deleteNotExistsWaiting() {
        given(waitingRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.removeWaiting(1L, "브라운"))
                .isInstanceOf(WaitingNotFoundException.class);
    }

    @Test
    @DisplayName("존재하는 예약 대기를 중복해서 저장하면, 예외가 발생한다.")
    void saveDuplicateWaiting() {
        LocalDate today = LocalDate.now();
        stubDependencies(savedTimeSlot, savedTheme);
        given(waitingRepository.isExists(any(Waiting.class))).willReturn(true);

        assertThatThrownBy(() -> waitingService.saveWaiting(createWaitingRequest(today)))
                .isInstanceOf(DuplicateWaitingException.class);
    }

    @Test
    @DisplayName("존재하는 예약에 대기를 추가하면, 예외가 발생한다.")
    void saveReservedWaiting() {
        LocalDate today = LocalDate.now();
        stubDependencies(savedTimeSlot, savedTheme);
        given(reservationRepository.findByDateAndTimeIdAndThemeId(today, 1L, 1L))
                .willReturn(Optional.of(createReservation(today)));

        assertThatThrownBy(() -> waitingService.saveWaiting(createWaitingRequest(today)))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("이미 지난 시간으로 대기를 추가하면, 예외가 발생한다.")
    void savePassedWaiting() {
        LocalDate today = LocalDate.now();
        TimeSlot pastTimeSlot = new TimeSlot(1L, LocalTime.of(0, 0));
        stubDependencies(pastTimeSlot, savedTheme);

        assertThatThrownBy(() -> waitingService.saveWaiting(createWaitingRequest(today)))
                .isInstanceOf(PastTimeException.class);
    }

    private void stubDependencies(TimeSlot timeSlot, Theme theme) {
        given(timeSlotRepository.findById(1L)).willReturn(Optional.of(timeSlot));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
    }

    private Waiting createWaitingEntity() {
        return new Waiting(1L, "브라운", LocalDate.now(), savedTimeSlot, savedTheme, 1);
    }

    private WaitingRequest createWaitingRequest(LocalDate date) {
        return new WaitingRequest("브라운", date, 1L, 1L);
    }

    private Reservation createReservation(LocalDate date) {
        return new Reservation(1L, "브라운", date, savedTimeSlot, savedTheme);
    }
}
