package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.payment.PaymentRepository;
import roomescape.domain.payment.PaymentStatus;
import roomescape.domain.payment.ReservationPayment;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.domain.reservationtime.dto.TimeSlot;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        time = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.of(1L, "테마1", "설명", "https://example.com/image.jpg");
    }

    @Nested
    @DisplayName("예약 생성 테스트")
    class CreateReservation {

        @Test
        void 정상_생성() {
            ReservationRequest request = new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            Reservation saved = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 1L, 1L)).thenReturn(false);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);
            when(paymentRepository.save(any(ReservationPayment.class))).thenReturn(paymentFor(saved.getId()));

            reservationService.createReservation(request);

            verify(reservationRepository, times(1)).save(any(Reservation.class));
        }

        @Test
        void 시간_id가_없으면_예외() {
            ReservationRequest request = new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), 99L, 1L);
            when(reservationTimeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TIME_ID_NOT_FOUND);
        }

        @Test
        void 테마_id가_없으면_예외() {
            ReservationRequest request = new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 99L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.THEME_ID_NOT_FOUND);
        }

        @Test
        void 과거_날짜면_예외() {
            ReservationRequest request = new ReservationRequest("유저1", LocalDate.of(2000, 1, 1), 1L, 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));

            assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_TIME_PASSED);
        }

        @Test
        void 중복_예약이면_예외() {
            ReservationRequest request = new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 1L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
        }

        @Test
        void 저장_중_중복_예약이면_예외() {
            ReservationRequest request = new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 1L, 1L)).thenReturn(false);
            when(reservationRepository.save(any(Reservation.class))).thenThrow(DuplicateKeyException.class);

            assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Nested
    @DisplayName("예약 조회 테스트")
    class GetReservations {

        @Test
        void 예약된_시간을_제외하고_반환() {
            LocalDate date = LocalDate.of(2099, 12, 31);
            ReservationTime t1 = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
            ReservationTime t2 = ReservationTime.of(2L, LocalTime.of(11, 0), LocalTime.of(12, 0));
            ReservationTime t3 = ReservationTime.of(3L, LocalTime.of(12, 0), LocalTime.of(13, 0));

            when(reservationTimeRepository.findAll()).thenReturn(List.of(t1, t2, t3));
            when(reservationRepository.findTimeByDateAndThemeId(date, 1L)).thenReturn(List.of(2L));

            List<TimeResponse> result = reservationService.getReservations(date, 1L);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(TimeResponse::id).containsExactly(1L, 3L);
        }

        @Test
        void 예약된_시간이_없으면_전체_반환() {
            LocalDate date = LocalDate.of(2099, 12, 31);
            ReservationTime t1 = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));

            when(reservationTimeRepository.findAll()).thenReturn(List.of(t1));
            when(reservationRepository.findTimeByDateAndThemeId(date, 1L)).thenReturn(List.of());

            List<TimeResponse> result = reservationService.getReservations(date, 1L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("예약 삭제 테스트")
    class DeleteReservation {

        @Test
        void 정상_삭제() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);
            TimeSlot canceledReservationSlot = TimeSlot.from(reservation);
            when(reservationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reservation));
            when(waitingRepository.findFirstByTimeSlotForUpdate(canceledReservationSlot)).thenReturn(Optional.empty());

            reservationService.deleteReservation(1L, "유저1");

            verify(reservationRepository, times(1)).deleteById(1L);
        }

        @Test
        void 예약_취소_시_1순위_대기를_예약으로_전환한다() {
            Reservation reservation = Reservation.of(1L, "예약자", LocalDate.of(2099, 12, 31), time, theme);
            Waiting waiting = Waiting.of(2L, "대기자1", LocalDate.of(2099, 12, 31), time, theme);
            TimeSlot canceledReservationSlot = TimeSlot.from(reservation);
            when(reservationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reservation));
            when(waitingRepository.findFirstByTimeSlotForUpdate(canceledReservationSlot)).thenReturn(Optional.of(waiting));
            Reservation promoted = Reservation.of(3L, "대기자1", LocalDate.of(2099, 12, 31), time, theme);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(promoted);
            when(paymentRepository.save(any(ReservationPayment.class))).thenReturn(paymentFor(promoted.getId()));

            reservationService.deleteReservation(1L, "예약자");

            InOrder inOrder = inOrder(reservationRepository, waitingRepository);
            inOrder.verify(reservationRepository).findByIdForUpdate(1L);
            inOrder.verify(reservationRepository).deleteById(1L);
            inOrder.verify(waitingRepository).findFirstByTimeSlotForUpdate(canceledReservationSlot);
            inOrder.verify(reservationRepository).save(any(Reservation.class));
            inOrder.verify(waitingRepository).deleteById(waiting.getId());
        }

        @Test
        void 존재하지_않는_id면_삭제하지_않음() {
            when(reservationRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

            reservationService.deleteReservation(99L, "유저1");

            verify(reservationRepository, times(0)).deleteById(99L);
        }
    }

    @Nested
    @DisplayName("본인 예약 조회 테스트")
    class GetMyReservations {

        @Test
        void 이름으로_조회() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);
            when(reservationRepository.findByName("유저1")).thenReturn(List.of(reservation));
            when(paymentRepository.findByReservationIds(List.of(1L)))
                .thenReturn(List.of(paymentFor(1L)));

            MyReservationsResponse response = reservationService.getMyReservations("유저1");

            assertAll(
                () -> assertThat(response.reservations()).hasSize(1),
                () -> assertThat(response.reservations().get(0).name()).isEqualTo("유저1"),
                () -> assertThat(response.reservations().get(0).themeName()).isEqualTo("테마1")
            );
        }

        @Test
        void 결과가_없으면_빈_리스트() {
            when(reservationRepository.findByName("없는유저")).thenReturn(List.of());
            when(paymentRepository.findByReservationIds(List.of())).thenReturn(List.of());

            MyReservationsResponse response = reservationService.getMyReservations("없는유저");

            assertThat(response.reservations()).isEmpty();
        }
    }

    @Nested
    @DisplayName("본인 예약 수정 테스트")
    class UpdateMyReservation {

        @Test
        void 정상_수정() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 30), time, theme);
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2099, 12, 31), 1L);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(reservationRepository.existsByIdForUpdate(1L)).thenReturn(true);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 1L, 1L)).thenReturn(false);

            reservationService.updateMyReservation(1L, request);

            InOrder inOrder = inOrder(reservationTimeRepository, reservationRepository);
            inOrder.verify(reservationTimeRepository).findById(1L);
            inOrder.verify(reservationRepository).existsByIdForUpdate(1L);
            verify(reservationRepository, times(1)).updateDateAndTime(1L, request.date(), 1L);
        }

        @Test
        void 존재하지_않는_예약이면_예외() {
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2099, 12, 31), 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(reservationRepository.existsByIdForUpdate(99L)).thenReturn(false);

            assertThatThrownBy(() -> reservationService.updateMyReservation(99L, request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_ID_NOT_FOUND);
        }

        @Test
        void 존재하지_않는_시간이면_예외() {
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2099, 12, 31), 99L);

            when(reservationTimeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.updateMyReservation(1L, request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TIME_ID_NOT_FOUND);
        }

        @Test
        void 권한이_없으면_예외() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 30), time, theme);
            ReservationFixRequest request = new ReservationFixRequest("다른유저", LocalDate.of(2099, 12, 31), 1L);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(reservationRepository.existsByIdForUpdate(1L)).thenReturn(true);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 1L, 1L)).thenReturn(false);

            assertThatThrownBy(() -> reservationService.updateMyReservation(1L, request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.UNAUTHORIZED_NAME);
        }

        @Test
        void 과거_날짜면_예외() {
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2000, 1, 1), 1L);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));

            assertThatThrownBy(() -> reservationService.updateMyReservation(1L, request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_TIME_PASSED);
        }

        @Test
        void 중복_예약이면_예외() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 30), time, theme);
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2099, 12, 31), 1L);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(reservationRepository.existsByIdForUpdate(1L)).thenReturn(true);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 1L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> reservationService.updateMyReservation(1L, request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
        }

        @Test
        void 수정_중_중복_예약이면_예외() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 30), time, theme);
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2099, 12, 31), 1L);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(reservationRepository.existsByIdForUpdate(1L)).thenReturn(true);
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 1L, 1L)).thenReturn(false);
            doThrow(DuplicateKeyException.class)
                .when(reservationRepository).updateDateAndTime(1L, request.date(), 1L);

            assertThatThrownBy(() -> reservationService.updateMyReservation(1L, request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private ReservationPayment paymentFor(Long reservationId) {
        return new ReservationPayment(
            reservationId,
            reservationId,
            "order-" + reservationId,
            null,
            10_000L,
            "idempotency-" + reservationId,
            PaymentStatus.PENDING
        );
    }
}
