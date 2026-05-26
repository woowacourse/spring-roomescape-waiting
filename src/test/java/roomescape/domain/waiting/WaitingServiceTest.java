package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @InjectMocks
    private WaitingService waitingService;

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        time = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.of(1L, "테마1", "설명", "https://example.com/image.jpg");
    }

    @Nested
    @DisplayName("예약 대기 생성 테스트")
    class CreateWaiting {

        @Test
        void 정상_생성() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            Waiting saved = Waiting.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(waitingRepository.existsByDateAndTimeIdAndThemeIdAndName(request.date(), 1L, 1L, request.name())).thenReturn(false);
            when(waitingRepository.save(any(Waiting.class))).thenReturn(saved);

            waitingService.createWaiting(request);

            verify(waitingRepository, times(1)).save(any(Waiting.class));
        }

        @Test
        void 시간_id가_없으면_예외() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 99L, 1L);
            when(reservationTimeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> waitingService.createWaiting(request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.TIME_ID_NOT_FOUND);
        }

        @Test
        void 테마_id가_없으면_예외() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 99L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> waitingService.createWaiting(request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.THEME_ID_NOT_FOUND);
        }

        @Test
        void 과거_날짜면_예외() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2000, 1, 1), 1L, 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));

            assertThatThrownBy(() -> waitingService.createWaiting(request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_TIME_PASSED);
        }

        @Test
        void 중복_대기이면_예외() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(waitingRepository.existsByDateAndTimeIdAndThemeIdAndName(request.date(), 1L, 1L,request.name())).thenReturn(true);

            assertThatThrownBy(() -> waitingService.createWaiting(request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_WAITING_NAME);
        }
    }

    @Nested
    @DisplayName("예약 대기 삭제 테스트")
    class DeleteWaiting {

        @Test
        void 정상_삭제() {
            when(waitingRepository.existsById(1L)).thenReturn(true);

            waitingService.deleteWaiting(1L);

            verify(waitingRepository, times(1)).deleteById(1L);
        }

        @Test
        void 존재하지_않는_id면_예외() {
            when(waitingRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> waitingService.deleteWaiting(99L))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.WAITING_ID_NOT_FOUND);
        }
    }
}