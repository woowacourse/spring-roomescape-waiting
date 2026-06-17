package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Order;
import roomescape.domain.OrderStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.AvailableDateResult;
import roomescape.dto.ReservationCreateCommand;
import roomescape.dto.ReservationModifyCommand;
import roomescape.dto.ReservationResult;
import roomescape.dto.ReservationStatus;
import roomescape.dto.ReservationTimeStatusResult;
import roomescape.dto.ReservationTimesWithStatus;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.OrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ReservationTimeRepository reservationTimeRepository;
    @Mock private ThemeRepository themeRepository;
    @Mock private WaitingListRepository waitingListRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationTime time;
    private Theme theme;
    private LocalDate futureDate;
    private Order order;

    @BeforeEach
    void setUp() {
        time = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.createWithId(1L, "테스트 테마", "테스트용 테마 설명입니다.", "https://test.com/img.jpg");
        futureDate = LocalDate.now().plusDays(1);
        order = Order.createWithId(1L, "order-test", 1000L, null, 1L, OrderStatus.PENDING);
    }

    @Nested
    class 예약_생성 {

        @Test
        void 성공() {
            ReservationCreateCommand request = new ReservationCreateCommand("오리", 1000L, futureDate, 1L, 1L);
            Reservation saved = Reservation.createWithId(1L, "오리", futureDate, time, theme);

            given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(false);
            given(reservationRepository.save(any(Reservation.class))).willReturn(saved);
            given(orderRepository.save(any(Order.class))).willReturn(order);

            ReservationResult response = reservationService.create(request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("오리");
            assertThat(response.date()).isEqualTo(futureDate);
            verify(reservationRepository).save(any(Reservation.class));
        }

        @Test
        void 테마가_없으면_예외발생() {
            ReservationCreateCommand request = new ReservationCreateCommand("오리", 1000L, futureDate, 1L, 1L);
            given(themeRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_NOT_FOUND);
            verify(reservationRepository, never()).save(any());
        }

        @Test
        void 시간이_없으면_예외발생() {
            ReservationCreateCommand request = new ReservationCreateCommand("오리", 1000L, futureDate, 1L, 1L);
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_NOT_FOUND);
            verify(reservationRepository, never()).save(any());
        }

        @Test
        void 과거_날짜면_예외발생() {
            LocalDate pastDate = LocalDate.now().minusDays(1);
            ReservationCreateCommand request = new ReservationCreateCommand("오리", 1000L, pastDate, 1L, 1L);

            given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

            assertThatThrownBy(() -> reservationService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
        }

        @Test
        void 중복_슬롯이면_예외발생() {
            ReservationCreateCommand request = new ReservationCreateCommand("오리", 1000L, futureDate, 1L, 1L);

            given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);

            assertThatThrownBy(() -> reservationService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_RESERVED);
        }
    }

    @Nested
    class 예약_변경 {

        @Test
        void 성공() {
            ReservationModifyCommand request = new ReservationModifyCommand(1L, "오리", futureDate.plusDays(1), 2L, 1L);
            Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
            ReservationTime newTime = ReservationTime.createWithId(2L, LocalTime.of(13, 0), LocalTime.of(14, 0));

            given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));
            given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(newTime));
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 2L, theme.getId())).willReturn(false);
            given(orderRepository.findByReservationId(1L)).willReturn(Optional.of(order));

            ReservationResult response = reservationService.modify(request);

            assertThat(response.date()).isEqualTo(request.date());
            assertThat(response.time().id()).isEqualTo(2L);
            verify(reservationRepository).update(any(Reservation.class));
        }

        @Test
        void 예약이_없으면_예외발생() {
            ReservationModifyCommand request = new ReservationModifyCommand(1L, "오리", futureDate.plusDays(1), 2L, 1L);
            given(reservationRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.modify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_FOUND);
        }

        @Test
        void 예약자명_불일치시_예외발생() {
            ReservationModifyCommand request = new ReservationModifyCommand(1L, "리오", futureDate.plusDays(1), 2L, 1L);
            Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);

            given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));

            assertThatThrownBy(() -> reservationService.modify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);
        }

        @Test
        void 변경할_날짜가_과거면_예외발생() {
            LocalDate pastDate = LocalDate.now().minusDays(1);
            ReservationModifyCommand request = new ReservationModifyCommand(1L, "오리", pastDate, 1L, 1L);
            Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);

            given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));
            given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

            assertThatThrownBy(() -> reservationService.modify(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
            verify(reservationRepository, never()).update(any());
        }

        @Test
        void 슬롯이_달라지면_대기자가_있으면_승격() {
            ReservationTime newTime = ReservationTime.createWithId(2L, LocalTime.of(13, 0), LocalTime.of(14, 0));
            ReservationModifyCommand request = new ReservationModifyCommand(1L, "오리", futureDate.plusDays(1), 2L, 1L);
            Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
            WaitingList waitingList = WaitingList.createWithId(1L, "거위", futureDate, theme, time, LocalDateTime.now().minusHours(1));

            given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));
            given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(newTime));
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 2L, 1L)).willReturn(false);
            given(waitingListRepository.findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(futureDate, time, theme))
                    .willReturn(Optional.of(waitingList));
            given(orderRepository.findByReservationId(1L)).willReturn(Optional.of(order));

            reservationService.modify(request);

            verify(reservationRepository).save(argThat(r ->
                    r.getName().equals("거위") &&
                    r.getReservationDate().getDate().equals(futureDate) &&
                    r.getTime().equals(time) &&
                    r.getTheme().equals(theme)
            ));
            verify(waitingListRepository).deleteById(1L);
        }

        @Test
        void 슬롯이_동일하면_대기자_승격_안함() {
            ReservationModifyCommand request = new ReservationModifyCommand(1L, "오리", futureDate, 1L, 1L);
            Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);

            given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));
            given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
            given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(false);
            given(orderRepository.findByReservationId(1L)).willReturn(Optional.of(order));

            reservationService.modify(request);

            verify(waitingListRepository, never()).findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(any(), any(), any());
        }
    }

    @Nested
    class 예약_삭제 {

        @Test
        void 성공_대기자_없음() {
            Reservation reservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(waitingListRepository.findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(futureDate, time, theme))
                    .willReturn(Optional.empty());

            reservationService.deleteWithValidation(1L, "오리");

            verify(reservationRepository).deleteById(1L);
            verify(reservationRepository, never()).save(any());
            verify(waitingListRepository, never()).deleteById(any());
        }

        @Test
        void 성공_대기자_있으면_승격() {
            Reservation reservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
            WaitingList waitingList = WaitingList.createWithId(1L, "거위", futureDate, theme, time, LocalDateTime.now().minusHours(1));

            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(waitingListRepository.findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(futureDate, time, theme))
                    .willReturn(Optional.of(waitingList));

            reservationService.deleteWithValidation(1L, "오리");

            verify(reservationRepository).deleteById(1L);
            verify(reservationRepository).save(argThat(r ->
                    r.getName().equals("거위") &&
                    r.getReservationDate().getDate().equals(futureDate) &&
                    r.getTime().equals(time) &&
                    r.getTheme().equals(theme)
            ));
            verify(waitingListRepository).deleteById(1L);
        }

        @Test
        void 예약이_없으면_예외발생() {
            given(reservationRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.deleteWithValidation(1L, "오리"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_FOUND);
            verify(reservationRepository, never()).deleteById(any());
        }

        @Test
        void 예약자명_불일치시_예외발생() {
            Reservation reservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.deleteWithValidation(1L, "거위"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);
        }

        @Test
        void 과거_예약이면_예외발생() {
            LocalDate pastDate = LocalDate.now().minusDays(1);
            Reservation reservation = Reservation.createWithId(1L, "오리", pastDate, time, theme);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

            assertThatThrownBy(() -> reservationService.deleteWithValidation(1L, "오리"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
            verify(reservationRepository, never()).deleteById(any());
        }
    }

    @Nested
    class 관리자_예약_삭제 {

        @Test
        void 성공_대기자_없음() {
            Reservation reservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(waitingListRepository.findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(futureDate, time, theme))
                    .willReturn(Optional.empty());

            reservationService.deleteAsAdmin(1L);

            verify(reservationRepository).deleteById(1L);
            verify(reservationRepository, never()).save(any());
            verify(waitingListRepository, never()).deleteById(any());
        }

        @Test
        void 성공_대기자_있으면_승격() {
            Reservation reservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
            WaitingList waitingList = WaitingList.createWithId(2L, "거위", futureDate, theme, time, LocalDateTime.now().minusHours(1));

            given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
            given(waitingListRepository.findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(futureDate, time, theme))
                    .willReturn(Optional.of(waitingList));

            reservationService.deleteAsAdmin(1L);

            verify(reservationRepository).deleteById(1L);
            verify(reservationRepository).save(argThat(r ->
                    r.getName().equals("거위") &&
                    r.getReservationDate().getDate().equals(futureDate) &&
                    r.getTime().equals(time) &&
                    r.getTheme().equals(theme)
            ));
            verify(waitingListRepository).deleteById(2L);
        }

        @Test
        void 예약이_없으면_예외발생() {
            given(reservationRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.deleteAsAdmin(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESERVATION_NOT_FOUND);
            verify(reservationRepository, never()).deleteById(any());
        }
    }

    @Nested
    class 예약_조회 {

        @Test
        void 사용자명으로_목록_조회() {
            String name = "검프";
            Reservation reservation = Reservation.createWithId(1L, name, futureDate, time, theme);
            given(reservationRepository.findByName(name)).willReturn(List.of(reservation));
            given(orderRepository.findByReservationId(1L)).willReturn(Optional.of(order));

            List<ReservationResult> responses = reservationService.getReservationsByName(name);

            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().name()).isEqualTo(name);
            assertThat(responses.getFirst().status()).isEqualTo(ReservationStatus.RESERVATION);
        }

        @Test
        void 날짜_테마로_예약시간_상태_목록_조회() {
            ReservationTimesWithStatus status1 = new ReservationTimesWithStatus(1L, LocalTime.of(10, 0), true);
            ReservationTimesWithStatus status2 = new ReservationTimesWithStatus(2L, LocalTime.of(12, 0), false);

            given(reservationRepository.findReservationTimeStatusesByDateAndThemeId(futureDate, 1L))
                    .willReturn(List.of(status1, status2));

            List<ReservationTimeStatusResult> responses = reservationService.getReservationTimeStatuses(futureDate, 1L);

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).reserved()).isTrue();
            assertThat(responses.get(1).reserved()).isFalse();
        }

        @Test
        void 예약_가능_날짜_목록_조회() {
            AvailableDateResult result = reservationService.getReservationOptions();

            assertThat(result.dates()).hasSize(14);
            assertThat(result.dates().getFirst()).isEqualTo(LocalDate.now());
            assertThat(result.dates().getLast()).isEqualTo(LocalDate.now().plusDays(13));
        }
    }
}
