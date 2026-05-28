package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationTimeService reservationTimeService;

    @Mock
    private ThemeService themeService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("period=7이면 오늘 제외 직전 7일 범위로 인기 테마를 조회한다.")
    void findPopularThemes_periodOf7_queriesLast7DaysExcludingToday() {
        LocalDate to = LocalDate.now().minusDays(1);
        LocalDate from = to.minusDays(7).plusDays(1);

        given(reservationRepository.findPopularThemes(from, to, 10))
                .willReturn(List.of(new PopularThemeQueryResult(1L, "테마", "설명", "url")));

        PopularThemesResult result = reservationService.findPopularThemes(7, 10);

        assertThat(result.popularThemes())
                .containsExactly(new PopularThemeQueryResult(1L, "테마", "설명", "url"));
        verify(reservationRepository).findPopularThemes(from, to, 10);
    }

    @Test
    @DisplayName("예약 생성 시 기존 예약과 중복되면 예외가 발생한다.")
    void makeReservation_duplicate() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        given(reservationTimeService.findById(1L)).willReturn(time);
        given(themeService.findById(1L)).willReturn(theme);
        given(reservationRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> reservationService.save(
                new ReservationCommand("브라운", LocalDate.now().plusDays(1), 1L, 1L)
        )).isInstanceOf(DuplicateException.class)
                .hasMessage(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("id에 해당하는 예약이 없으면 예외가 발생한다.")
    void deleteReservationById_not_found() {
        assertThatThrownBy(() -> reservationService.deleteById(1L, "브라운"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("본인 예약 변경 시 변경하려는 예약과 동일한 예약이 있으면 예외가 발생한다.")
    void updateReservation_duplicate() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        LocalDate futureDate = LocalDate.now().plusDays(1);

        given(reservationRepository.findById(1L))
                .willReturn(Optional.of(new Reservation(1L, "브라운", futureDate, time, theme)));
        given(reservationTimeService.findById(1L)).willReturn(time);
        given(reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                futureDate, 1L, 1L, 1L
        )).willReturn(true);

        assertThatThrownBy(() -> reservationService.update(
                new ReservationUpdateCommand(futureDate, 1L), 1L, "브라운"
        )).isInstanceOf(DuplicateException.class)
                .hasMessage(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("이름에 해당하는 예약들을 조회한다.")
    void findAllByName_existingName_returnsReservationsWithStatus() {
        ReservationTime firstTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime secondTime = new ReservationTime(2L, LocalTime.of(11, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        List<ReservationWithStatusResult> expected = List.of(
                new ReservationWithStatusResult(
                        1L, "브라운", LocalDate.of(2026, 5, 15), firstTime, theme, "reserved", 0L
                ),
                new ReservationWithStatusResult(
                        2L, "브라운", LocalDate.of(2026, 5, 16), secondTime, theme, "waiting", 2L
                )
        );
        given(reservationRepository.findAllByNameWithStatus("브라운")).willReturn(expected);

        List<ReservationWithStatusResult> result = reservationService.findAllByName("브라운");

        assertThat(result).containsExactlyElementsOf(expected);
    }
}
