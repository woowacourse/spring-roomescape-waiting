package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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

    @InjectMocks
    ReservationService reservationService;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    Clock clock;

    @Mock
    ReservationTimeService reservationTimeService;

    @Mock
    ThemeService themeService;

    @Test
    @DisplayName("인기 테마 조회 시 period=7이면 오늘 제외 직전 7일 범위로 조회한다.")
    void findPopularThemes_periodOf7_queriesLast7DaysExcludingToday() {
        //given
        given(clock.instant()).willReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        given(
                reservationRepository.findPopularThemes(
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 7), 10
                )
        ).willReturn(
                List.of(
                        new PopularThemeQueryResult(
                                1L,
                                "테마",
                                "설명",
                                "url"
                        )
                )
        );

        //when
        PopularThemesResult result = reservationService.findPopularThemes(7, 10);

        //then
        assertThat(result.popularThemes()).containsExactly(
                new PopularThemeQueryResult(
                        1L,
                        "테마",
                        "설명",
                        "url")
        );

        verify(reservationRepository).findPopularThemes(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7),
                10
        );
    }

    @Test
    @DisplayName("예약 생성 시, 기존에 이미 동일한 예약이 있으면 예외가 발생한다.")
    void makeReservation_duplicate() {
        //given
        given(clock.instant()).willReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        given(reservationRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        //when & then
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");

        assertThatThrownBy(() -> reservationService.save(
                new ReservationCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                ), time, theme
        )).isInstanceOf(DuplicateException.class)
                .hasMessage(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("id에 해당하는 예약이 없으면 예외가 발생한다.")
    void deleteReservationById_not_found() {
        assertThatThrownBy(() -> reservationService.deleteById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("본인 예약 변경 시, 기존에 변경하려는 예약과 동일한 예약이 있으면 예외가 발생한다.")
    void updateReservation_duplicate() {
        //given
        given(clock.instant()).willReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        given(clock.getZone()).willReturn(ZoneId.systemDefault());

        given(reservationRepository.findById(1L))
                .willReturn(Optional.of(
                        new Reservation(
                                1L,
                                "브라운",
                                LocalDate.of(2026, 5, 15),
                                new ReservationTime(1L, LocalTime.of(10, 0)),
                                new Theme(1L, "이름", "설명", "thumbnailUrl")
                        )
                ));

        given(reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                LocalDate.of(2026, 5, 15), 1L, 1L, 1L)
        ).willReturn(true);

        //when & then
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        assertThatThrownBy(() -> reservationService.update(
                new ReservationUpdateCommand(
                        LocalDate.of(2026, 5, 15), 1L
                ), 1L, time
        )).isInstanceOf(DuplicateException.class)
                .hasMessage(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("이름에 해당하는 예약들을 조회한다.")
    void findAllByName_existingName_returnsReservationsWithStatus() {
        //given
        // Mock native query result combining reserved and waiting entries
        given(reservationRepository.findAllByNameWithStatus("브라운"))
                .willReturn(List.of(
                        new ReservationWithStatusResult(
                                1L,
                                "브라운",
                                LocalDate.of(2026, 5, 15),
                                new ReservationTime(1L, LocalTime.of(10, 0)),
                                new Theme(1L, "이름", "설명", "thumbnailUrl"),
                                "reserved",
                                0L
                        ),
                        new ReservationWithStatusResult(
                                2L,
                                "브라운",
                                LocalDate.of(2026, 5, 16),
                                new ReservationTime(2L, LocalTime.of(11, 0)),
                                new Theme(1L, "이름", "설명", "thumbnailUrl"),
                                "waiting",
                                2L
                        )
                ));

        //when
        List<ReservationWithStatusResult> result = reservationService.findAllByName("브라운");

        //then
        assertThat(result.size()).isEqualTo(2);

        assertThat(result).containsExactly(
                new ReservationWithStatusResult(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl"),
                        "reserved",
                        0L
                ), new ReservationWithStatusResult(
                        2L,
                        "브라운",
                        LocalDate.of(2026, 5, 16),
                        new ReservationTime(2L, LocalTime.of(11, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl"),
                        "waiting",
                        2L
                )
        );
    }
}
