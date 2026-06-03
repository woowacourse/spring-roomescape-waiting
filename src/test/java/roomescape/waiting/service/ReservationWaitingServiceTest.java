package roomescape.waiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.lenient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.ForbiddenException;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;
import roomescape.waiting.exception.ReservationWaitingErrorCode;
import roomescape.waiting.service.dto.ReservationWaitingCommand;
import roomescape.waiting.service.dto.ReservationWaitingResult;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    private Reservation buildReservation(Long id, String name, ReservationSlot slot) {
        return new Reservation(id, name, slot, slot.date().atStartOfDay());
    }

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeService reservationTimeService;

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @BeforeEach
    void setUp() {
        lenient().when(reservationTimeService.getByIdForUpdate(any()))
                .thenReturn(new ReservationTime(1L, LocalTime.of(10, 0)));
        lenient().when(themeService.findById(any()))
                .thenReturn(new Theme(1L, "테마", "설명", "url"));
    }

    @Test
    @DisplayName("예약 대기 생성 시, 대기 신청 대상 예약이 존재하지 않으면 NotFoundException이 발생한다.")
    void save_NoTargetReservation_ThrowsNotFoundException() {
        // given
        ReservationWaitingCommand command = new ReservationWaitingCommand(
                "브라운", LocalDate.now().plusDays(1), 1L, 1L
        );
        given(reservationRepository.findBySlot(any(ReservationSlot.class))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.save(command, java.time.LocalDateTime.now()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationWaitingErrorCode.TARGET_RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약 대기 생성 시, 동일 슬롯에 본인이 이미 예약해 둔 상태라면 InvalidBusinessStateException이 발생한다.")
    void save_AlreadyReservedByRequester_ThrowsInvalidBusinessStateException() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation targetReservation = buildReservation(1L, "포비", new ReservationSlot(futureDate, time, theme));

        ReservationWaitingCommand command = new ReservationWaitingCommand(
                "브라운", futureDate, 1L, 1L
        );

        given(reservationRepository.findBySlot(any(ReservationSlot.class))).willReturn(
                Optional.of(targetReservation));
        given(reservationRepository.hasBookingAtSameTime(any(Reservation.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.save(command, java.time.LocalDateTime.now()))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
    }

    @Test
    @DisplayName("예약 대기 생성 시, 동일 슬롯에 본인이 이미 대기를 걸어둔 상태라면 InvalidBusinessStateException이 발생한다.")
    void save_AlreadyWaitingByRequester_ThrowsInvalidBusinessStateException() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation targetReservation = buildReservation(1L, "포비", new ReservationSlot(futureDate, time, theme));

        ReservationWaitingCommand command = new ReservationWaitingCommand(
                "브라운", futureDate, 1L, 1L
        );

        given(reservationRepository.findBySlot(any(ReservationSlot.class))).willReturn(
                Optional.of(targetReservation));
        given(reservationRepository.hasBookingAtSameTime(any(Reservation.class))).willReturn(false);
        given(reservationWaitingRepository.hasWaitingAtSameTime(any(ReservationWaiting.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.save(command, java.time.LocalDateTime.now()))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.ALREADY_RESERVED.getMessage());
    }

    @Test
    @DisplayName("예약 대기 생성 시, 과거 날짜의 슬롯이면 InvalidBusinessStateException이 발생한다.")
    void save_PastDate_ThrowsInvalidBusinessStateException() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation targetReservation = buildReservation(1L, "포비", new ReservationSlot(pastDate, time, theme));

        ReservationWaitingCommand command = new ReservationWaitingCommand(
                "브라운", pastDate, 1L, 1L
        );

        lenient().when(reservationRepository.findBySlot(any(ReservationSlot.class))).thenReturn(
                Optional.of(targetReservation));
        lenient().when(reservationRepository.hasBookingAtSameTime(any(Reservation.class))).thenReturn(false);
        lenient().when(reservationWaitingRepository.hasWaitingAtSameTime(any(ReservationWaiting.class)))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.save(command, java.time.LocalDateTime.now()))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 대기 생성 시, 오늘이지만 이미 지난 시각의 슬롯이면 InvalidBusinessStateException이 발생한다.")
    void save_TodayDateButPastTime_ThrowsInvalidBusinessStateException() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusHours(1);
        ReservationTime time = new ReservationTime(1L, pastTime);
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation targetReservation = buildReservation(1L, "포비", new ReservationSlot(today, time, theme));

        ReservationWaitingCommand command = new ReservationWaitingCommand("브라운", today, 1L, 1L);

        lenient().when(reservationRepository.findBySlot(any(ReservationSlot.class))).thenReturn(
                Optional.of(targetReservation));
        lenient().when(reservationRepository.hasBookingAtSameTime(any(Reservation.class))).thenReturn(false);
        lenient().when(reservationWaitingRepository.hasWaitingAtSameTime(any(ReservationWaiting.class)))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.save(command, java.time.LocalDateTime.now()))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 대기 생성 시, 데이터베이스 제약 조건 위반 등으로 중복 생성 시 ConflictException이 발생한다.")
    void save_DuplicateWaitingInDb_ThrowsConflictException() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation targetReservation = buildReservation(1L, "포비", new ReservationSlot(futureDate, time, theme));

        ReservationWaitingCommand command = new ReservationWaitingCommand(
                "브라운", futureDate, 1L, 1L
        );

        given(reservationRepository.findBySlot(any(ReservationSlot.class))).willReturn(
                Optional.of(targetReservation));
        given(reservationRepository.hasBookingAtSameTime(any(Reservation.class))).willReturn(false);
        given(reservationWaitingRepository.hasWaitingAtSameTime(any(ReservationWaiting.class))).willReturn(false);
        given(reservationWaitingRepository.save(any())).willThrow(
                new DataIntegrityViolationException("Duplicate key error"));

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.save(command, java.time.LocalDateTime.now()))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ReservationWaitingErrorCode.DUPLICATE_WAITING.getMessage());
    }

    @Test
    @DisplayName("예약 대기 삭제 시, 존재하지 않는 대기 아이디인 경우 NotFoundException이 발생한다.")
    void deleteById_NotExistWaiting_ThrowsNotFoundException() {
        // given
        given(reservationWaitingRepository.findById(any()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.deleteById(1L, "브라운", java.time.LocalDateTime.now()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약 대기 삭제 시, 본인의 대기 내역이 아닌 경우 ForbiddenException이 발생한다.")
    void deleteById_NotOwner_ThrowsForbiddenException() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(futureDate, time, theme),
                futureDate.atStartOfDay());

        given(reservationWaitingRepository.findById(any())).willReturn(Optional.of(waiting));

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.deleteById(1L, "포비", java.time.LocalDateTime.now()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ReservationWaitingErrorCode.AUTHORIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("예약 대기 삭제 시, 대기 슬롯 날짜가 이미 지난 경우 InvalidBusinessStateException이 발생한다.")
    void deleteById_PastDate_ThrowsInvalidBusinessStateException() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(pastDate, time, theme),
                pastDate.atStartOfDay());

        given(reservationWaitingRepository.findById(any())).willReturn(Optional.of(waiting));

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.deleteById(1L, "브라운", java.time.LocalDateTime.now()))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 대기 삭제 시, 오늘이지만 이미 대기 슬롯 시간이 지난 시점이라면 InvalidBusinessStateException이 발생한다.")
    void deleteById_TodayDateButPastTime_ThrowsInvalidBusinessStateException() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.now().minusHours(1);
        ReservationTime time = new ReservationTime(1L, pastTime);
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(today, time, theme),
                today.atStartOfDay());

        given(reservationWaitingRepository.findById(any())).willReturn(Optional.of(waiting));

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.deleteById(1L, "브라운", java.time.LocalDateTime.now()))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationWaitingErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 대기 삭제 시, 데이터베이스 삭제 결과 건수가 0이면 NotFoundException이 발생한다.")
    void deleteById_DeleteCountZero_ThrowsNotFoundException() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(futureDate, time, theme),
                futureDate.atStartOfDay());

        given(reservationWaitingRepository.findById(any())).willReturn(Optional.of(waiting));
        willThrow(new NotFoundException(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage()))
                .given(reservationWaitingRepository).delete(any(ReservationWaiting.class));

        // when & then
        assertThatThrownBy(() -> reservationWaitingService.deleteById(1L, "브라운", java.time.LocalDateTime.now()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationWaitingErrorCode.WAITING_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약 대기 생성 시, 조건이 유효하면 정상적으로 생성에 성공한다.")
    void save_Success() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation targetReservation = buildReservation(1L, "포비", new ReservationSlot(futureDate, time, theme));

        ReservationWaitingCommand command = new ReservationWaitingCommand("브라운", futureDate, 1L, 1L);
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(futureDate, time, theme),
                futureDate.atStartOfDay());

        given(reservationRepository.findBySlot(any(ReservationSlot.class))).willReturn(
                Optional.of(targetReservation));
        given(reservationRepository.hasBookingAtSameTime(any(Reservation.class))).willReturn(false);
        given(reservationWaitingRepository.hasWaitingAtSameTime(any(ReservationWaiting.class))).willReturn(false);
        given(reservationWaitingRepository.save(any())).willReturn(waiting);

        // when
        ReservationWaitingResult result = reservationWaitingService.save(command, java.time.LocalDateTime.now());

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("예약 대기 삭제 시, 조건이 유효하면 정상적으로 삭제에 성공한다.")
    void deleteById_Success() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationWaiting waiting = new ReservationWaiting(1L, "브라운", new ReservationSlot(futureDate, time, theme),
                futureDate.atStartOfDay());

        given(reservationWaitingRepository.findById(any())).willReturn(Optional.of(waiting));
        willDoNothing().given(reservationWaitingRepository).delete(any(ReservationWaiting.class));

        // when & then
        assertDoesNotThrow(() -> reservationWaitingService.deleteById(1L, "브라운", java.time.LocalDateTime.now()));
    }
}
