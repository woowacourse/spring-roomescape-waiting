package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.InvalidWaitingPrerequisiteException;
import roomescape.exception.PastTimeException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private SlotService slotService;

    private WaitingService waitingService;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;
    private Slot savedSlot;

    @BeforeEach
    void setUp() {
        savedTimeSlot = new TimeSlot(1L, LocalTime.of(10, 0));
        savedTheme = new Theme(1L, "이름", "설명", "test.com");
        savedSlot = new Slot(1L, LocalDate.now().plusDays(1), savedTimeSlot, savedTheme);
        waitingService = new WaitingService(waitingRepository, reservationRepository, slotService);
    }

    @Test
    @DisplayName("유효한 요청으로 예약 대기를 생성하고 반환한다.")
    void saveWaiting() {
        Waiting waiting = new Waiting(1L, "브라운", savedSlot, 1);
        given(slotService.findSlotOrNull(any(), anyLong(), anyLong())).willReturn(savedSlot);
        given(reservationRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .willReturn(Optional.of(new Reservation(1L, "포비", savedSlot)));
        given(waitingRepository.isExists(any(Waiting.class))).willReturn(false);
        given(waitingRepository.save(any(Waiting.class))).willReturn(waiting);

        Waiting result = waitingService.saveWaiting(createWaitingRequest(LocalDate.now().plusDays(1)));
        assertThat(result.getName()).isEqualTo("브라운");
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
        given(slotService.findSlotOrNull(any(), anyLong(), anyLong())).willReturn(savedSlot);
        given(reservationRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .willReturn(Optional.of(new Reservation(1L, "포비", savedSlot)));
        given(waitingRepository.isExists(any(Waiting.class))).willReturn(true);
        assertThatThrownBy(() -> waitingService.saveWaiting(createWaitingRequest(LocalDate.now())))
                .isInstanceOf(DuplicateWaitingException.class);
    }

    @Test
    @DisplayName("자신의 예약에 대기를 추가하면, 예외가 발생한다.")
    void saveReservedWaiting() {
        given(slotService.findSlotOrNull(any(), anyLong(), anyLong())).willReturn(savedSlot);
        given(reservationRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .willReturn(Optional.of(createReservation()));
        assertThatThrownBy(() -> waitingService.saveWaiting(createWaitingRequest(LocalDate.now())))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("존재하는 예약이지만 이미 지난 시간으로 대기를 추가하면, 예외가 발생한다.")
    void savePassedWaiting() {
        LocalDate today = LocalDate.now();
        TimeSlot pastTime = new TimeSlot(1L, LocalTime.of(0, 0));
        Slot pastSlot = new Slot(2L, today, pastTime, savedTheme);
        stubOtherPersonReservation(pastSlot);
        assertThatThrownBy(() -> waitingService.saveWaiting(createWaitingRequest(today)))
                .isInstanceOf(PastTimeException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약에 대기를 추가하면, 예외가 발생한다.")
    void saveNotExistsReservationWaiting() {
        given(slotService.findSlotOrNull(any(), anyLong(), anyLong())).willReturn(savedSlot);
        given(reservationRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> waitingService.saveWaiting(createWaitingRequest(LocalDate.now())))
                .isInstanceOf(InvalidWaitingPrerequisiteException.class);
    }

    private void stubOtherPersonReservation(Slot slot) {
        given(slotService.findSlotOrNull(any(), anyLong(), anyLong())).willReturn(slot);
        Reservation reservation = new Reservation(2L, "포비", slot);
        given(reservationRepository.findByDateAndTimeIdAndThemeId(any(), any(), any()))
                .willReturn(Optional.of(reservation));
    }

    private Waiting createWaitingEntity() {
        return new Waiting(1L, "브라운", savedSlot, 1);
    }

    private WaitingRequest createWaitingRequest(LocalDate date) {
        return new WaitingRequest("브라운", date, 1L, 1L);
    }

    private Reservation createReservation() {
        return new Reservation(1L, "브라운", savedSlot);
    }
}
