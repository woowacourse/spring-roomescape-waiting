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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.payment.domain.PaymentRepository;
import roomescape.domain.reservation.ReservationSummary;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.reservationtime.dto.TimeResponse;
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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        time = ReservationTime.of(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.of(1L, "테마1", "설명", "https://example.com/image.jpg", 50_000L);
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
            when(reservationRepository.existsBySlot(any(ReservationSlot.class))).thenReturn(false);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

            ReservationResponse response = reservationService.createReservation(request);

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
        void 중복_예약이면_예외() {
            ReservationRequest request = new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsBySlot(any(ReservationSlot.class))).thenReturn(true);

            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
        }

        @Test
        void save_시_DuplicateKeyException_발생하면_DUPLICATE_RESERVATION_NAME_예외로_변환() {
            ReservationRequest request = new ReservationRequest("유저1", LocalDate.of(2099, 12, 31), 1L, 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
            when(reservationRepository.existsBySlot(any(ReservationSlot.class))).thenReturn(false);
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
            when(reservationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.deleteById(1L)).thenReturn(1);
            when(waitingRepository.findAllBySlotForUpdate(any(ReservationSlot.class))).thenReturn(List.of());

            reservationService.deleteReservation(1L);

            verify(reservationRepository, times(1)).deleteById(1L);
        }

        @Test
        void 대기자가_있으면_첫_번째_대기가_예약으로_승격된다() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 31), time, theme);
            Waiting waiting1 = Waiting.of(10L, "대기자1", LocalDate.of(2099, 12, 31), time, theme);
            Waiting waiting2 = Waiting.of(11L, "대기자2", LocalDate.of(2099, 12, 31), time, theme);
            Reservation promoted = Reservation.of(20L, "대기자1", LocalDate.of(2099, 12, 31), time, theme);
            when(reservationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.deleteById(1L)).thenReturn(1);
            when(waitingRepository.findAllBySlotForUpdate(any(ReservationSlot.class))).thenReturn(List.of(waiting1, waiting2));
            when(reservationRepository.save(any(Reservation.class))).thenReturn(promoted);

            reservationService.deleteReservation(1L);

            verify(reservationRepository, times(1)).deleteById(1L);
            verify(waitingRepository, times(1)).deleteById(10L);
            verify(reservationRepository, times(1)).save(any(Reservation.class));
        }

        @Test
        void 존재하지_않는_id면_예외() {
            when(reservationRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.deleteReservation(99L))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.RESERVATION_ID_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("본인 예약 조회 테스트")
    class GetMyReservations {

        @Test
        void 이름으로_조회() {
            ReservationSummary summary = new ReservationSummary(1L, "유저1", LocalDate.of(2099, 12, 31), time.getStartAt(), "테마1", ReservationStatus.CONFIRMED, null, null, null);
            when(reservationRepository.findByName("유저1")).thenReturn(List.of(summary));

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
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.existsBySlot(any(ReservationSlot.class))).thenReturn(false);

            reservationService.updateMyReservation(1L, request);

            InOrder inOrder = inOrder(reservationTimeRepository, reservationRepository);
            inOrder.verify(reservationTimeRepository).findById(1L);
            inOrder.verify(reservationRepository).findById(1L);
            verify(reservationRepository, times(1)).update(any(Reservation.class));
        }

        @Test
        void 존재하지_않는_예약이면_예외() {
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2099, 12, 31), 1L);
            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

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
        void 중복_예약이면_예외() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 30), time, theme);
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2099, 12, 31), 1L);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.existsBySlot(any(ReservationSlot.class))).thenReturn(true);

            assertThatThrownBy(() -> reservationService.updateMyReservation(1L, request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
        }

        @Test
        void update_시_DuplicateKeyException_발생하면_DUPLICATE_RESERVATION_NAME_예외로_변환() {
            Reservation reservation = Reservation.of(1L, "유저1", LocalDate.of(2099, 12, 30), time, theme);
            ReservationFixRequest request = new ReservationFixRequest("유저1", LocalDate.of(2099, 12, 31), 1L);

            when(reservationTimeRepository.findById(1L)).thenReturn(Optional.of(time));
            when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
            when(reservationRepository.existsBySlot(any(ReservationSlot.class))).thenReturn(false);
            doThrow(DuplicateKeyException.class).when(reservationRepository).update(any(Reservation.class));

            assertThatThrownBy(() -> reservationService.updateMyReservation(1L, request))
                    .isInstanceOf(RoomescapeException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}
