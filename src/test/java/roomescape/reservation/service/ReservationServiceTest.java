package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.domain.ReservationWaitingRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeService reservationTimeService;

    @Mock
    private ThemeService themeService;

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 생성 시 슬롯 중복으로 DB 예외가 발생하면 ConflictException을 던진다.")
    void save_duplicateSlot_throwsConflictException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationCommand command = new ReservationCommand("브라운", LocalDate.now().plusDays(1), 1L, 1L);

        given(reservationTimeService.getById(1L)).willReturn(time);
        given(themeService.findById(1L)).willReturn(theme);
        given(reservationRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        // when & then
        assertThatThrownBy(() -> reservationService.save(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("예약 생성 시 존재하지 않는 시간 ID를 입력하면 NotFoundException을 던진다.")
    void save_nonExistentTime_throwsNotFoundException() {
        // given
        ReservationCommand command = new ReservationCommand("브라운", LocalDate.now().plusDays(1), 999L, 1L);
        given(reservationTimeService.getById(999L)).willThrow(new NotFoundException("Time not found"));

        // when & then
        assertThatThrownBy(() -> reservationService.save(command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약 생성 시 존재하지 않는 테마 ID를 입력하면 NotFoundException을 던진다.")
    void save_nonExistentTheme_throwsNotFoundException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationCommand command = new ReservationCommand("브라운", LocalDate.now().plusDays(1), 1L, 999L);

        given(reservationTimeService.getById(1L)).willReturn(time);
        given(themeService.findById(999L)).willThrow(new NotFoundException("Theme not found"));

        // when & then
        assertThatThrownBy(() -> reservationService.save(command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약 생성 시 본인의 동시간대 기존 예약이 존재하면 InvalidBusinessStateException을 던진다.")
    void save_duplicateUserReservation_throwsInvalidBusinessStateException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationCommand command = new ReservationCommand("브라운", date, 1L, 1L);

        given(reservationTimeService.getById(1L)).willReturn(time);
        given(themeService.findById(1L)).willReturn(theme);
        given(reservationRepository.existsByDateAndTimeIdAndName(date, 1L, "브라운")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.save(command))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 생성 시 본인이 해당 시간대에 이미 대기 등록을 해둔 상태라면 InvalidBusinessStateException을 던진다.")
    void save_userAlreadyWaitingAtSameTime_throwsInvalidBusinessStateException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationCommand command = new ReservationCommand("브라운", date, 1L, 1L);

        given(reservationTimeService.getById(1L)).willReturn(time);
        given(themeService.findById(1L)).willReturn(theme);
        // 예약은 없지만(false) 대기열에 있음(true)
        given(reservationRepository.existsByDateAndTimeIdAndName(date, 1L, "브라운")).willReturn(false);
        given(reservationWaitingRepository.existsByDateAndTimeIdAndName(date, 1L, "브라운")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.save(command))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME.getMessage());
    }

    @Test
    @DisplayName("과거 날짜로 예약을 생성하려 하면 InvalidBusinessStateException을 던진다.")
    void save_expiredDate_throwsInvalidBusinessStateException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationCommand command = new ReservationCommand("브라운", LocalDate.now().minusDays(1), 1L, 1L);

        given(reservationTimeService.getById(1L)).willReturn(time);
        given(themeService.findById(1L)).willReturn(theme);

        // when & then
        assertThatThrownBy(() -> reservationService.save(command))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("수정 대상 예약이 존재하지 않으면 NotFoundException을 던진다.")
    void update_nonExistentReservation_throwsNotFoundException() {
        // given
        ReservationUpdateCommand command = new ReservationUpdateCommand(LocalDate.now().plusDays(1), 1L);
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.update(command, 1L, "브라운"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("본인의 예약이 아닌 건을 수정하려 하면 ForbiddenException을 던진다.")
    void update_notOwner_throwsForbiddenException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "포비",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme));
        ReservationUpdateCommand command = new ReservationUpdateCommand(LocalDate.now().plusDays(2), 1L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.update(command, 1L, "브라운"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("이미 만료된(지난) 예약건을 수정하려 시도하면 InvalidBusinessStateException을 던진다.")
    void update_expiredOriginalReservation_throwsInvalidBusinessStateException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().minusDays(1), time, theme));
        ReservationUpdateCommand command = new ReservationUpdateCommand(LocalDate.now().plusDays(1), 1L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.update(command, 1L, "브라운"))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 수정 시 지정한 새로운 날짜가 과거인 경우 InvalidBusinessStateException을 던진다.")
    void update_expiredNewDate_throwsInvalidBusinessStateException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme));
        ReservationUpdateCommand command = new ReservationUpdateCommand(LocalDate.now().minusDays(1), 1L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeService.getById(1L)).willReturn(time);

        // when & then
        assertThatThrownBy(() -> reservationService.update(command, 1L, "브라운"))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 수정 시 존재하지 않는 시간 ID를 입력하면 NotFoundException을 던진다.")
    void update_nonExistentTime_throwsNotFoundException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme));
        ReservationUpdateCommand command = new ReservationUpdateCommand(LocalDate.now().plusDays(1), 999L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeService.getById(999L)).willThrow(new NotFoundException("Time not found"));

        // when & then
        assertThatThrownBy(() -> reservationService.update(command, 1L, "브라운"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약 수정 시 본인이 같은 시간대에 다른 예약을 해둔 상태라면 InvalidBusinessStateException을 던진다.")
    void update_duplicateUserBooking_throwsInvalidBusinessStateException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        LocalDate targetDate = LocalDate.now().plusDays(2);
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme));
        ReservationUpdateCommand command = new ReservationUpdateCommand(targetDate, 1L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeService.getById(1L)).willReturn(time);
        given(reservationRepository.existsByDateAndTimeIdAndNameAndIdNot(targetDate, 1L, "브라운", 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.update(command, 1L, "브라운"))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 수정 시 본인이 해당 시간대에 이미 대기 등록을 해둔 상태라면 InvalidBusinessStateException을 던진다.")
    void update_userAlreadyWaitingAtSameTime_throwsInvalidBusinessStateException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        LocalDate targetDate = LocalDate.now().plusDays(2);
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme));
        ReservationUpdateCommand command = new ReservationUpdateCommand(targetDate, 1L);

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));
        given(reservationTimeService.getById(1L)).willReturn(time);
        given(reservationRepository.existsByDateAndTimeIdAndNameAndIdNot(targetDate, 1L, "브라운", 1L)).willReturn(false);
        given(reservationWaitingRepository.existsByDateAndTimeIdAndName(targetDate, 1L, "브라운")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.update(command, 1L, "브라운"))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.ALREADY_RESERVED_OR_WAITING_AT_SAME_TIME.getMessage());
    }

    @Test
    @DisplayName("삭제하려는 예약이 존재하지 않으면 NotFoundException을 던진다.")
    void delete_nonExistentReservation_throwsNotFoundException() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.deleteById(1L, "브라운"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("삭제하려는 예약이 본인 소유가 아니면 ForbiddenException을 던진다.")
    void delete_notOwner_throwsForbiddenException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "포비",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme));

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.deleteById(1L, "브라운"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(ReservationErrorCode.AUTHORIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("과거 날짜의 예약을 삭제하려 하면 InvalidBusinessStateException을 던진다.")
    void delete_expiredReservation_throwsInvalidBusinessStateException() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().minusDays(1), time, theme));

        given(reservationRepository.findById(1L)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.deleteById(1L, "브라운"))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("예약 삭제 시, 동시간대 중복 예약이 있는 대기자 W1은 건너뛰고 중복이 없는 W2를 승격시킨다.")
    void delete_skipsDoubleBookedWaitingAndPromotesNext() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        LocalDate date = LocalDate.now().plusDays(1);
        Reservation targetReservation = new Reservation(1L, "브라운", new ReservationSlot(date, time, theme));

        ReservationWaiting w1 = new ReservationWaiting(10L, "중복대기자", new ReservationSlot(date, time, theme));
        ReservationWaiting w2 = new ReservationWaiting(20L, "정상대기자", new ReservationSlot(date, time, theme));

        given(reservationRepository.findById(1L)).willReturn(Optional.of(targetReservation));
        given(reservationWaitingRepository.findAllByDateAndTimeIdAndThemeIdForUpdate(date, 1L, 1L))
                .willReturn(List.of(w1, w2));

        given(reservationRepository.existsByDateAndTimeIdAndName(date, 1L, "중복대기자")).willReturn(true);
        given(reservationRepository.existsByDateAndTimeIdAndName(date, 1L, "정상대기자")).willReturn(false);

        // when
        reservationService.deleteById(1L, "브라운");

        // then
        then(reservationRepository).should().delete(targetReservation);
        then(reservationWaitingRepository).should().findAllByDateAndTimeIdAndThemeIdForUpdate(date, 1L, 1L);
        then(reservationWaitingRepository).should().delete(w2);
        then(reservationRepository).should().save(Reservation.of("정상대기자", date, time, theme));
    }


    @Test
    @DisplayName("존재하지 않는 ID로 예약을 조회하면 NotFoundException을 던진다.")
    void getById_nonExistentId_throwsNotFoundException() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.getById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("인기 테마를 올바른 기간 범위로 조회한다.")
    void findPopularThemes_queriesCorrectRange() {
        // given
        LocalDate to = LocalDate.now().minusDays(1);
        LocalDate from = to.minusDays(7).plusDays(1);
        given(reservationRepository.findPopularThemes(from, to, 10))
                .willReturn(List.of(new PopularThemeQueryResult(1L, "테마", "설명", "url")));

        // when
        PopularThemesResult result = reservationService.findPopularThemes(7, 10);

        // then
        assertThat(result.popularThemes()).hasSize(1);
        then(reservationRepository).should().findPopularThemes(from, to, 10);
    }
}
