package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import roomescape.global.exception.DuplicateException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.exception.ReservationErrorCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import roomescape.reservation.domain.Reservation;


import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ReservationTimeRepository reservationTimeRepository;

    @Mock
    ReservationWaitingRepository reservationWaitingRepository;

    @Mock
    ThemeRepository themeRepository;

    @Mock
    Clock clock;

    @InjectMocks
    ReservationService reservationService;

    @DisplayName("인기 테마 조회 시 period=7이면 오늘 제외 직전 7일 범위로 조회한다.")
    @Test
    void findPopularThemesRange() {
        //given
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(
                reservationRepository.findPopularThemes(
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 7), 10
                )
        ).thenReturn(
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

    @DisplayName("예약 생성 시, 기존에 이미 동일한 예약이 있으면 예외가 발생한다.")
    @Test
    void makeReservation_duplicate() {
        //given
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(10, 0))));

        when(themeRepository.findById(any()))
                .thenReturn(Optional.of(new Theme(1L, "이름", "설명", "thumbnailUrl")));

        when(reservationRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        //when & then
        assertThatThrownBy(() -> reservationService.save(
                new ReservationCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(DuplicateException.class)
                .hasMessage(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @DisplayName("id에 해당하는 예약이 없으면 예외가 발생한다.")
    @Test
    void deleteReservationById_not_found() {
        assertThatThrownBy(() -> reservationService.deleteById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(ReservationErrorCode.RESERVATION_NOT_FOUND.getMessage());
    }

    @DisplayName("본인 예약 변경 시, 기존에 변경하려는 예약과 동일한 예약이 있으면 예외가 발생한다.")
    @Test
    void updateReservation_duplicate() {
        //given
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(10, 0))));

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(
                        new Reservation(
                                1L,
                                "브라운",
                                LocalDate.of(2026, 5, 15),
                                new ReservationTime(1L, LocalTime.of(10, 0)),
                                new Theme(1L, "이름", "설명", "thumbnailUrl")
                        )
                ));

        when(reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                LocalDate.of(2026, 5, 15), 1L, 1L, 1L)
        ).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationService.update(
                new ReservationUpdateCommand(
                        LocalDate.of(2026, 5, 15), 1L
                ), 1L
        )).isInstanceOf(DuplicateException.class)
                .hasMessage(ReservationErrorCode.DUPLICATE_RESERVATION.getMessage());
    }

    @DisplayName("이름에 해당하는 예약들을 조회한다.")
    @Test
    void findReservationsByNameTest() {
        //given
        when(reservationRepository.findAllByName("브라운"))
                .thenReturn(List.of(new Reservation(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl")
                        ))
                );

        when(reservationWaitingRepository.findAllByName("브라운"))
                .thenReturn(List.of(new ReservationWaiting(
                                1L,
                                "브라운",
                                LocalDate.of(2026, 5, 15),
                                new ReservationTime(2L, LocalTime.of(11, 0)),
                                new Theme(1L, "이름", "설명", "thumbnailUrl")
                        ))
                );

        when(reservationWaitingRepository.countByDateAndTimeIdAndThemeIdAndIdLessThan(
                any(), any(), any(), any()
        )).thenReturn(0L);

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
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        new ReservationTime(2L, LocalTime.of(11, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl"),
                        "waiting",
                        1L
                )
        );
    }
}
