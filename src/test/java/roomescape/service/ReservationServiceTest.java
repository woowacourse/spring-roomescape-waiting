package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;
import roomescape.repository.dto.ReservationTimesWithStatus;
import roomescape.service.dto.ReservationAvailableEvent;
import roomescape.service.dto.ReservationStatus;
import roomescape.service.dto.command.ReservationCreateCommand;
import roomescape.service.dto.command.ReservationDeleteCommand;
import roomescape.service.dto.command.ReservationModifyCommand;
import roomescape.service.dto.result.ReservationResult;
import roomescape.service.dto.result.ReservationTimeStatusResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private WaitingListRepository waitingListRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationTime time;
    private Theme theme;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        time = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.createWithId(1L, "테스트 테마", "테스트용 테마 설명입니다.", "https://test.com/img.jpg", 30000L);
        futureDate = LocalDate.now().plusDays(1);
    }

    @Test
    void 예약_생성() {
        // given
        ReservationCreateCommand request = new ReservationCreateCommand("오리", futureDate, 1L, 1L);

        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));
        given(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(false);

        Reservation savedReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
        given(reservationRepository.save(any(Reservation.class))).willReturn(savedReservation);

        // when
        ReservationResult response = reservationService.create(request);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("오리");
        assertThat(response.date()).isEqualTo(futureDate);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void 과거_날짜로_예약_생성_시도시_예외발생() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationCreateCommand request = new ReservationCreateCommand("오리", pastDate, 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
    }

    @Test
    void 중복예약_시도시_예외발생() {
        // given
        ReservationCreateCommand request = new ReservationCreateCommand("오리", futureDate, 1L, 1L);

        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_RESERVED);
    }

    @Test
    void 예약_대기가_존재하는데_예약_생성을_시도할시_예외발생() {
        // given
        ReservationCreateCommand request = new ReservationCreateCommand("오리", futureDate, 1L, 1L);

        given(reservationTimeRepository.findById(1L)).willReturn(Optional.of(time));
        given(themeRepository.findById(1L)).willReturn(Optional.of(theme));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(false);
        given(waitingListRepository.existsByDateAndTimeIdAndThemeId(futureDate, 1L, 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUEUED_WAITING_LIST);
    }

    @Test
    void 변경을_시도하는_사용자명과_예약자명_일치시_예약_변경_및_대기_승인_이벤트_발생() {
        // given
        ReservationModifyCommand request = new ReservationModifyCommand(1L, "오리", futureDate.plusDays(1), 2L);
        Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
        ReservationTime newTime = ReservationTime.createWithId(2L, LocalTime.of(13, 0), LocalTime.of(14, 0));

        given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(newTime));
        given(reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), 2L, theme.getId())).willReturn(false);
        given(waitingListRepository.existsByDateAndTimeIdAndThemeId(request.date(), 2L, theme.getId())).willReturn(false);

        // when
        ReservationResult response = reservationService.modify(request);

        // then
        assertThat(response.date()).isEqualTo(request.date());
        assertThat(response.time().id()).isEqualTo(2L);
        verify(reservationRepository).updateDateAndTime(any(Reservation.class));
        verify(eventPublisher).publishEvent(any(ReservationAvailableEvent.class));
    }

    @Test
    void 변경을_시도하는_사용자명과_예약자명_불일치시_예외발생_및_대기_승인_이벤트_미발생() {
        // given
        ReservationModifyCommand request = new ReservationModifyCommand(1L, "리오", futureDate.plusDays(1), 2L);
        Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));

        // when & them
        assertThatThrownBy(() -> reservationService.modify(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);
        verify(eventPublisher, never()).publishEvent(any(ReservationAvailableEvent.class));
    }

    @Test
    void 변경을_시도하는_건에_이미_예약이_존재할시_예외발생_및_대기_승인_이벤트_미발생() {
        // given
        LocalDate newDate = futureDate.plusDays(1);
        Long newTimeId = 2L;
        ReservationModifyCommand request = new ReservationModifyCommand(1L, "오리", newDate, newTimeId);
        Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));
        ReservationTime newTime = ReservationTime.createWithId(newTimeId, LocalTime.of(13, 0), LocalTime.of(14, 0));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(newTime));
        given(reservationRepository.existsByDateAndTimeIdAndThemeId(newDate, newTimeId, theme.getId())).willReturn(true);

        // when & them
        assertThatThrownBy(() -> reservationService.modify(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_RESERVED);
        verify(eventPublisher, never()).publishEvent(any(ReservationAvailableEvent.class));
    }

    @Test
    void 변경을_시도하는_건에_이미_예약대기가_존재할시_예외발생_및_대기_승인_이벤트_미발생() {
        // given
        LocalDate newDate = futureDate.plusDays(1);
        Long newTimeId = 2L;
        ReservationModifyCommand request = new ReservationModifyCommand(1L, "오리", newDate, newTimeId);
        Reservation originalReservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(originalReservation));
        ReservationTime newTime = ReservationTime.createWithId(newTimeId, LocalTime.of(13, 0), LocalTime.of(14, 0));
        given(reservationTimeRepository.findById(2L)).willReturn(Optional.of(newTime));
        given(reservationRepository.existsByDateAndTimeIdAndThemeId(newDate, newTimeId, theme.getId())).willReturn(false);
        given(waitingListRepository.existsByDateAndTimeIdAndThemeId(newDate, newTimeId, theme.getId())).willReturn(true);

        // when & them
        assertThatThrownBy(() -> reservationService.modify(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUEUED_WAITING_LIST);
        verify(eventPublisher, never()).publishEvent(any(ReservationAvailableEvent.class));
    }

    @Test
    void 삭제를_시도하는_사용자명과_예약자명_일치시_예약_삭제_및_대기_승인_이벤트_발생() {
        // given
        Reservation reservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationRepository.deleteById(1L)).willReturn(true);
        ReservationDeleteCommand correctDeleteCommand = new ReservationDeleteCommand(1L, "오리");

        // when
        reservationService.deleteWithValidation(correctDeleteCommand);

        // then
        verify(reservationRepository).deleteById(1L);
        verify(eventPublisher).publishEvent(any(ReservationAvailableEvent.class));
    }

    @Test
    void 삭제를_시도하는_사용자명과_예약자명_불일치시_예외발생_및_대기_승인_이벤트_미발생() {
        // given
        Reservation reservation = Reservation.createWithId(1L, "오리", futureDate, time, theme);
        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        ReservationDeleteCommand wrongDeleteCommand = new ReservationDeleteCommand(1L, "거위");

        // when & then
        assertThatThrownBy(() -> reservationService.deleteWithValidation(wrongDeleteCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);
        verify(eventPublisher, never()).publishEvent(any(ReservationAvailableEvent.class));
    }

    @Test
    void 특정_날짜와_테마의_예약시간_상태_목록_조회() {
        // given
        ReservationTimesWithStatus status1 = new ReservationTimesWithStatus(1L, LocalTime.of(10, 0), true);
        ReservationTimesWithStatus status2 = new ReservationTimesWithStatus(2L, LocalTime.of(12, 0), false);

        given(reservationRepository.findReservationTimeStatusesByDateAndThemeId(futureDate, 1L))
                .willReturn(List.of(status1, status2));

        // when
        List<ReservationTimeStatusResult> responses = reservationService.getReservationTimeStatuses(futureDate, 1L);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).reserved()).isTrue();
        assertThat(responses.get(1).reserved()).isFalse();
    }

    @Test
    void 사용자명으로_예약_목록_조회() {
        // given
        String name = "검프";
        Reservation reservation = Reservation.createWithId(1L, name, futureDate, time, theme);

        given(reservationRepository.findByName(name)).willReturn(List.of(reservation));

        // when
        List<ReservationResult> responses = reservationService.getReservationsByName(name);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().name()).isEqualTo(name);
        assertThat(responses.getFirst().status()).isEqualTo(ReservationStatus.RESERVATION);
    }
}
