package roomescape.domain.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waiting.dto.MyWaitingsResponse;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;
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

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private WaitingService waitingService;

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        time = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.of(1L, "테마1", "설명", "https://example.com/image.jpg", 50_000L);
    }

    @Nested
    @DisplayName("예약 대기 생성 테스트")
    class CreateWaiting {

        @Test
        void 정상_생성() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            Reservation reservation = Reservation.of(1L, "예약자", LocalDate.of(2099, 12, 31), time, theme);
            Waiting saved = Waiting.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.findBySlot(any(ReservationSlot.class))).thenReturn(Optional.of(reservation));
            when(waitingRepository.findAllBySlot(any(ReservationSlot.class))).thenReturn(List.of());
            when(waitingRepository.save(any(Waiting.class))).thenReturn(saved);

            WaitingResponse response = waitingService.createWaiting(request);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(1L),
                    () -> assertThat(response.name()).isEqualTo("유저1"),
                    () -> assertThat(response.date()).isEqualTo(LocalDate.of(2099, 12, 31)),
                    () -> assertThat(response.timeId()).isEqualTo(1L),
                    () -> assertThat(response.themeId()).isEqualTo(1L)
            );
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
        void 예약이_없는_슬롯이면_예외() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.findBySlot(any(ReservationSlot.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> waitingService.createWaiting(request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
        }

        @Test
        void 중복_대기이면_예외() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            Reservation reservation = Reservation.of(1L, "예약자", LocalDate.of(2099, 12, 31), time, theme);
            Waiting existing = Waiting.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.findBySlot(any(ReservationSlot.class))).thenReturn(Optional.of(reservation));
            when(waitingRepository.findAllBySlot(any(ReservationSlot.class))).thenReturn(List.of(existing));

            assertThatThrownBy(() -> waitingService.createWaiting(request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_WAITING_NAME);
        }

        @Test
        void 대기자가_예약자와_같으면_WAITING_NOT_AVAILABLE_예외() {
            WaitingRequest request = new WaitingRequest("예약자", LocalDate.of(2099, 12, 31), 1L, 1L);
            Reservation reservation = Reservation.of(1L, "예약자", LocalDate.of(2099, 12, 31), time, theme);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.findBySlot(any(ReservationSlot.class))).thenReturn(Optional.of(reservation));
            when(waitingRepository.findAllBySlot(any(ReservationSlot.class))).thenReturn(List.of());

            assertThatThrownBy(() -> waitingService.createWaiting(request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.WAITING_NOT_AVAILABLE);
        }

        @Test
        void save_시_DuplicateKeyException_발생하면_DUPLICATE_WAITING_NAME_예외로_변환() {
            WaitingRequest request = new WaitingRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            Reservation reservation = Reservation.of(1L, "예약자", LocalDate.of(2099, 12, 31), time, theme);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.findBySlot(any(ReservationSlot.class))).thenReturn(Optional.of(reservation));
            when(waitingRepository.findAllBySlot(any(ReservationSlot.class))).thenReturn(List.of());
            when(waitingRepository.save(any(Waiting.class))).thenThrow(DuplicateKeyException.class);

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

    @Nested
    @DisplayName("본인 예약 대기 조회 테스트")
    class GetMyWaitings {

        @Test
        void 이름으로_조회() {
            Waiting myWaiting = Waiting.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);
            when(waitingRepository.findByName("유저1")).thenReturn(List.of(myWaiting));
            when(waitingRepository.findAllBySlot(any(ReservationSlot.class))).thenReturn(List.of(myWaiting));

            MyWaitingsResponse response = waitingService.getMyWaitings("유저1");

            assertAll(
                    () -> assertThat(response.waitings()).hasSize(1),
                    () -> assertThat(response.waitings().getFirst().name()).isEqualTo("유저1"),
                    () -> assertThat(response.waitings().getFirst().themeName()).isEqualTo("테마1"),
                    () -> assertThat(response.waitings().getFirst().waitingNumber()).isEqualTo(1)
            );
        }

        @Test
        void 결과가_없으면_빈_리스트() {
            when(waitingRepository.findByName("없는유저")).thenReturn(List.of());

            MyWaitingsResponse response = waitingService.getMyWaitings("없는유저");

            assertThat(response.waitings()).isEmpty();
        }
    }
}