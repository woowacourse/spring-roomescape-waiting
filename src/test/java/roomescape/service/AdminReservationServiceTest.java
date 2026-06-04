package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;
import roomescape.service.dto.ReservationWithWaitingOrder;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;

@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    private static final ReservationTime VALID_TIME = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final Theme VALID_THEME = new Theme(
            1L, "무인도 탈출", "설명", "https://example.com/thumb.jpg");
    private static final LocalDate VALID_DATE = LocalDate.now().plusDays(1);
    private static final ReservationCreateCommand VALID_COMMAND = new ReservationCreateCommand(
            "루드비코", VALID_DATE, 1L, 1L);

    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @InjectMocks
    private AdminReservationService reservationService;

    @Nested
    @DisplayName("예약 및 대기 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("슬롯이 비어있으면 예약으로 확정한다")
        void reserveWhenSlotEmpty() {
            given(reservationTimeRepository.findById(anyLong())).willReturn(Optional.of(VALID_TIME));
            given(themeRepository.findById(anyLong())).willReturn(Optional.of(VALID_THEME));
            given(reservationRepository.hasReservationOnSlot(any(), anyLong(), anyLong())).willReturn(false);
            given(reservationRepository.save(any())).willReturn(
                    new ReservationWithWaitingOrder(1L, "루드비코", VALID_DATE, VALID_TIME, VALID_THEME, 0L));

            ReservationResult result = reservationService.reserveOnSlot(VALID_COMMAND);

            assertThat(result.waitingOrder()).isZero();
            verify(reservationRepository, times(1)).save(any());
            verify(waitingRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("슬롯이 꽉 찼으면 대기로 등록한다")
        void waitWhenSlotFull() {
            given(reservationTimeRepository.findById(anyLong())).willReturn(Optional.of(VALID_TIME));
            given(themeRepository.findById(anyLong())).willReturn(Optional.of(VALID_THEME));
            given(reservationRepository.hasReservationOnSlot(any(), anyLong(), anyLong())).willReturn(true);
            given(waitingRepository.save(any())).willReturn(
                    new Reservation(1L, "루드비코", VALID_DATE, VALID_TIME, VALID_THEME));
            given(waitingRepository.countWaitingsBefore(any())).willReturn(2L);

            ReservationResult result = reservationService.reserveOnSlot(VALID_COMMAND);

            assertThat(result.waitingOrder()).isEqualTo(3L);
            verify(reservationRepository, times(0)).save(any());
            verify(waitingRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("본인이 이미 예약 또는 대기 중이면 예외가 발생한다")
        void failWhenDuplicate() {
            given(reservationTimeRepository.findById(anyLong())).willReturn(Optional.of(VALID_TIME));
            given(themeRepository.findById(anyLong())).willReturn(Optional.of(VALID_THEME));
            given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(anyString(), any(), anyLong(), anyLong()))
                    .willReturn(true);

            assertThrows(ReservationConflictException.class, () -> reservationService.reserveOnSlot(VALID_COMMAND));
        }
    }

    @Nested
    @DisplayName("예약 삭제 및 대기 승격 테스트")
    class DeleteTest {

        @Test
        @DisplayName("예약을 삭제할 때 대기자가 있으면 자동 승격시킨다")
        void promoteWhenDelete() {
            Reservation deletedRes = new Reservation(1L, "기존예약자", VALID_DATE, VALID_TIME, VALID_THEME);
            Reservation firstWaiting = new Reservation(2L, "대기1번", VALID_DATE, VALID_TIME, VALID_THEME);

            given(reservationRepository.findById(1L)).willReturn(Optional.of(deletedRes));
            given(waitingRepository.findFirstWaiting(any(), anyLong(), anyLong())).willReturn(Optional.of(firstWaiting));

            reservationService.delete(1L);

            verify(reservationRepository, times(1)).deleteById(1L);
            verify(waitingRepository, times(1)).deleteById(2L);
            verify(reservationRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 예약을 삭제하면 예외가 발생한다")
        void failWhenDeleteNotFound() {
            given(reservationRepository.findById(1L)).willReturn(Optional.empty());

            assertThrows(ReservationNotFoundException.class, () -> reservationService.delete(1L));
        }
    }
}
