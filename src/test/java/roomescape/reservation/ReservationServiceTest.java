package roomescape.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservationtime.ReservationTime;
import roomescape.slot.Slot;
import roomescape.slot.application.SlotAssembler;
import roomescape.theme.Theme;
import roomescape.waiting.infrastructure.WaitingRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final long MEMBER_ID = 1L;
    private static final long OTHER_MEMBER_ID = 2L;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SlotAssembler slotAssembler;

    @Mock
    private WaitingRepository waitingRepository;

    private ReservationService reservationService;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-05-01T00:00:00Z"),
            ZoneId.systemDefault()
    );

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository,
                waitingRepository,
                slotAssembler,
                clock
        );
    }

    private Reservation reservation(
            Long reservationId,
            Long memberId,
            LocalDate date,
            Long themeId,
            Long timeId,
            LocalTime startAt,
            Long slotId
    ) {
        return Reservation.of(reservationId, memberId, slot(slotId, date, themeId, timeId, startAt));
    }

    private Slot slot(Long slotId, LocalDate date, Long themeId, Long timeId, LocalTime startAt) {
        return Slot.of(
                slotId,
                date,
                new ReservationTime(timeId, startAt),
                new Theme(themeId, "theme", "description", "thumbnail")
        );
    }

    @Test
    @DisplayName("유저는 본인 예약 삭제에 성공한다.")
    void deleteById_user_success() {
        long reservationId = 1L;
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0), 10L
        );
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatCode(() -> reservationService.deleteByIdForUser(reservationId, MEMBER_ID))
                .doesNotThrowAnyException();
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    @DisplayName("매니저는 예약 삭제에 성공한다.")
    void deleteById_manager_success() {
        long reservationId = 1L;
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0), 10L
        );
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatCode(() -> reservationService.deleteById(reservationId))
                .doesNotThrowAnyException();
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    @DisplayName("유저는 타인 예약 삭제를 할 수 없다.")
    void deleteById_user_other_member_forbidden() {
        long reservationId = 1L;
        Reservation oldReservation = reservation(
                reservationId, OTHER_MEMBER_ID, LocalDate.of(2026, 6, 1), 1L, 1L, LocalTime.of(10, 0), 10L
        );
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatThrownBy(() -> reservationService.deleteByIdForUser(reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("유저는 본인 예약 수정에 성공한다.")
    void update_user_success() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0), 10L
        );
        Slot newSlot = slot(99L, request.date(), 3L, request.timeId(), LocalTime.of(14, 0));
        Reservation updated = Reservation.of(reservationId, MEMBER_ID, newSlot);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation), Optional.of(updated));
        when(slotAssembler.assembleExisting(request.date(), request.timeId(), 3L))
                .thenReturn(newSlot);
        when(reservationRepository.existsBySlotIdAndIdNot(99L, reservationId)).thenReturn(false);
        when(reservationRepository.updateSlotById(reservationId, 99L)).thenReturn(1);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(updated));

        ReservationSaveResponse response = reservationService.updateForUser(request, reservationId, MEMBER_ID);

        assertThat(response.id()).isEqualTo(reservationId);
        verify(reservationRepository).updateSlotById(reservationId, 99L);
    }

    @Test
    @DisplayName("매니저는 예약 수정에 성공한다.")
    void update_manager_success() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0), 10L
        );
        Slot newSlot = slot(99L, request.date(), 3L, request.timeId(), LocalTime.of(14, 0));
        Reservation updated = Reservation.of(reservationId, MEMBER_ID, newSlot);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation), Optional.of(updated));
        when(slotAssembler.assembleExisting(request.date(), request.timeId(), 3L))
                .thenReturn(newSlot);
        when(reservationRepository.existsBySlotIdAndIdNot(99L, reservationId)).thenReturn(false);
        when(reservationRepository.updateSlotById(reservationId, 99L)).thenReturn(1);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(updated));

        ReservationSaveResponse response = reservationService.update(request, reservationId);

        assertThat(response.id()).isEqualTo(reservationId);
        verify(reservationRepository).updateSlotById(reservationId, 99L);
    }

    @Test
    @DisplayName("유저는 타인 예약 수정을 할 수 없다.")
    void update_user_other_member_forbidden() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        Reservation oldReservation = reservation(
                reservationId, OTHER_MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0), 10L
        );

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).updateSlotById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("변경할 슬롯에 다른 예약이 있으면 예약 수정에 실패한다.")
    void update_duplicated_reservation_fail() {
        long reservationId = 4L;
        long newSlotId = 99L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0), 10L
        );
        Slot newSlot = slot(newSlotId, request.date(), 3L, request.timeId(), LocalTime.of(14, 0));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(slotAssembler.assembleExisting(request.date(), request.timeId(), 3L))
                .thenReturn(newSlot);
        when(reservationRepository.existsBySlotIdAndIdNot(newSlotId, reservationId)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).updateSlotById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("변경할 슬롯에 대기열이 있으면 예약 수정에 실패한다.")
    void update_waiting_exists_fail() {
        long reservationId = 4L;
        long newSlotId = 99L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 6, 1), 3L, 3L, LocalTime.of(11, 0), 10L
        );
        Slot newSlot = slot(newSlotId, request.date(), 3L, request.timeId(), LocalTime.of(14, 0));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));
        when(slotAssembler.assembleExisting(request.date(), request.timeId(), 3L))
                .thenReturn(newSlot);
        when(reservationRepository.existsBySlotIdAndIdNot(newSlotId, reservationId)).thenReturn(false);
        when(waitingRepository.existsBySlotId(newSlotId)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).updateSlotById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("예약 시간이 과거면 수정에 실패한다.")
    void update_past_time_fail() {
        long reservationId = 4L;
        ReservationUpdateRequest request = new ReservationUpdateRequest(LocalDate.of(2026, 6, 2), 4L);
        Reservation oldReservation = reservation(
                reservationId, MEMBER_ID, LocalDate.of(2026, 4, 30), 3L, 3L, LocalTime.of(11, 0), 10L
        );

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(oldReservation));

        assertThatThrownBy(() -> reservationService.updateForUser(request, reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).updateSlotById(anyLong(), anyLong());
    }
}
