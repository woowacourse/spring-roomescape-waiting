package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
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
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.InvalidReservationDateValueException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.exception.ReservationSlotHasWaitingException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.PopularThemeQueryResult;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationCommand;
import roomescape.reservation.service.dto.ReservationUpdateCommand;
import roomescape.reservation.service.dto.ReservationWithStatusResult;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.InvalidTimeStartAtValueException;
import roomescape.time.exception.TimeNotFoundException;
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

    @DisplayName("예약 생성 시, 기존에 이미 동일한 예약이 있으면 예외가 발생한다.")
    @Test
    void makeReservationTest_duplicate() {
        //given
        when(reservationRepository.existByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationService.makeReservation(
                new ReservationCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(DuplicateReservationException.class);
    }

    @DisplayName("예약 생성 시, 동일한 슬롯의 예약 대기가 존재하면 예외가 발생한다.")
    @Test
    void makeReservationTest_ReservationSlotHasWaiting() {
        //given
        when(reservationRepository.existByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(false);

        when(reservationWaitingRepository.existsByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationService.makeReservation(
                new ReservationCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(ReservationSlotHasWaitingException.class);
    }

    @DisplayName("예약 생성 시, 시간이 없으면 예외가 발생한다.")
    @Test
    void makeReservationTest_time_not_found() {
        //given
        when(reservationRepository.existByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(false);

        when(reservationWaitingRepository.existsByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(false);

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationService.makeReservation(
                new ReservationCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("예약 생성 시, 시간이 유효하지 않으면 예외가 발생한다.")
    @Test
    void makeReservationTest_invalid_time() {
        //given
        when(reservationRepository.existByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(false);

        when(reservationWaitingRepository.existsByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(false);

        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(time));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 20)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        //when & then
        assertThatThrownBy(() -> reservationService.makeReservation(
                new ReservationCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(InvalidReservationDateValueException.class);
    }

    @DisplayName("예약 생성 시, 시간이 유효하지 않으면 예외가 발생한다.")
    @Test
    void makeReservationTest_theme_not_found() {
        //given
        when(reservationRepository.existByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(false);

        when(reservationWaitingRepository.existsByDateAndTimeIdAndThemeId(any(), any(), any()
        )).thenReturn(false);

        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(time));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(themeRepository.findById(any()))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationService.makeReservation(
                new ReservationCommand(
                        "브라운", LocalDate.of(2026, 5, 15), 1L, 1L
                )
        )).isInstanceOf(ThemeNotFoundException.class);
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

        when(reservationWaitingRepository.countByReservationDateAndTimeIdAndThemeIdAndIdLessThan(
                any(), any(), any(), any()
        )).thenReturn(0L);

        //when
        List<ReservationWithStatusResult> result = reservationService.findReservationsByName("브라운");

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

    @DisplayName("인기 테마 조회 시 period=7이면 오늘 제외 직전 7일 범위로 조회한다.")
    @Test
    void findPopularThemesTest() {
        //given
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationRepository.findPopularThemes(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7), 10
                )).thenReturn(List.of(
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

    @DisplayName("예약 변경 시, 기존 슬롯의 첫 번째 예약 대기가 예약으로 승격된다.")
    @Test
    void updateReservationTest_success() {
        //given
        ReservationTime originalTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime updatedTime = new ReservationTime(2L, LocalTime.of(11, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        LocalDate originalDate = LocalDate.of(2026, 5, 15);
        LocalDate updatedDate = LocalDate.of(2026, 5, 16);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(
                        new Reservation(
                                1L,
                                "브라운",
                                originalDate,
                                originalTime,
                                theme
                        )
                ));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(updatedTime));

        when(reservationRepository.existByDateAndTimeIdAndThemeIdExceptId(any(), any(), any(), any()))
                .thenReturn(false);

        when(reservationWaitingRepository.existsByDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(false);

        when(reservationWaitingRepository.findFirstByReservationDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.of(new ReservationWaiting(
                        1L,
                        "포비",
                        originalDate,
                        originalTime,
                        theme))
                );

        when(reservationWaitingRepository.deleteById(any()))
                .thenReturn(1);

        //when
        reservationService.updateReservation(
                new ReservationUpdateCommand(
                        updatedDate, 2L
                ), 1L
        );

        //then
        verify(reservationRepository).update(argThat(reservation ->
                reservation.getId().equals(1L)
                        && reservation.getName().equals("브라운")
                        && reservation.getDate().equals(updatedDate)
                        && reservation.getTime().equals(updatedTime)
                        && reservation.getTheme().equals(theme)
        ));
        verify(reservationWaitingRepository).findFirstByReservationDateAndTimeIdAndThemeId(originalDate, 1L, 1L);
        verify(reservationWaitingRepository).deleteById(1L);
        verify(reservationRepository).save(argThat(reservation ->
                reservation.getName().equals("포비")
                        && reservation.getDate().equals(originalDate)
                        && reservation.getTime().equals(originalTime)
                        && reservation.getTheme().equals(theme)
        ));
    }

    @DisplayName("예약 변경 시, id에 해당하는 예약이 없으면 예외가 발생한다.")
    @Test
    void updateReservationTest_reservation_not_found() {
        //given
        when(reservationRepository.findById(any()))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 2L), 1L
        )).isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("예약 변경 시, 기존 예약이 이미 지난 예약이면 예외가 발생한다.")
    @Test
    void updateReservationTest_original_expired() {
        //given
        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 16),
                        new ReservationTime(1L, LocalTime.of(11, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl")
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 17)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 17), 2L), 1L
        )).isInstanceOf(InvalidReservationDateValueException.class);
    }

    @DisplayName("예약 변경 시, 변경할 시간 id에 해당하는 시간이 없으면 예외가 발생한다.")
    @Test
    void updateReservationTest_time_not_found() {
        //given
        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 16),
                        new ReservationTime(1L, LocalTime.of(11, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl")
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 2L), 1L
        )).isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("예약 변경 시, 변경 후 예약 시간이 이미 지났으면 예외가 발생한다.")
    @Test
    void updateReservationTest_updated_time_expired() {
        //given
        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 16),
                        new ReservationTime(1L, LocalTime.of(11, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl")
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 15)
                        .atTime(10, 30)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(new ReservationTime(2L, LocalTime.of(10, 0))));

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 15), 2L), 1L
        )).isInstanceOf(InvalidTimeStartAtValueException.class);
    }

    @DisplayName("예약 변경 시, 변경할 슬롯에 이미 예약이 있으면 예외가 발생한다.")
    @Test
    void updateReservationTest_duplicate() {
        //given
        ReservationTime updatedTime = new ReservationTime(2L, LocalTime.of(11, 0));

        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl")
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(updatedTime));

        when(reservationRepository.existByDateAndTimeIdAndThemeIdExceptId(any(), any(), any(), any()))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 2L), 1L
        )).isInstanceOf(DuplicateReservationException.class);
    }

    @DisplayName("예약 변경 시, 변경할 슬롯에 예약 대기가 있으면 예외가 발생한다.")
    @Test
    void updateReservationTest_slot_has_waiting() {
        //given
        ReservationTime updatedTime = new ReservationTime(2L, LocalTime.of(11, 0));

        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl")
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(updatedTime));

        when(reservationRepository.existByDateAndTimeIdAndThemeIdExceptId(any(), any(), any(), any()))
                .thenReturn(false);

        when(reservationWaitingRepository.existsByDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(true);

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 2L), 1L
        )).isInstanceOf(ReservationSlotHasWaitingException.class);
    }

    @DisplayName("예약 변경 시, 기존 슬롯의 예약 대기 승격 중 중복이 발생하면 예외가 발생한다.")
    @Test
    void updateReservationTest_promote_duplicate() {
        //given
        ReservationTime originalTime = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime updatedTime = new ReservationTime(2L, LocalTime.of(11, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        LocalDate originalDate = LocalDate.of(2026, 5, 15);

        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        originalDate,
                        originalTime,
                        theme
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        when(reservationTimeRepository.findById(any()))
                .thenReturn(Optional.of(updatedTime));

        when(reservationRepository.existByDateAndTimeIdAndThemeIdExceptId(any(), any(), any(), any()))
                .thenReturn(false);

        when(reservationWaitingRepository.existsByDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(false);

        when(reservationWaitingRepository.findFirstByReservationDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.of(new ReservationWaiting(
                        1L,
                        "포비",
                        originalDate,
                        originalTime,
                        theme
                )));

        when(reservationRepository.save(any()))
                .thenThrow(DataIntegrityViolationException.class);

        when(reservationWaitingRepository.deleteById(any()))
                .thenReturn(1);

        //when & then
        assertThatThrownBy(() -> reservationService.updateReservation(
                new ReservationUpdateCommand(LocalDate.of(2026, 5, 16), 2L), 1L
        )).isInstanceOf(DuplicateReservationException.class);
    }

    @DisplayName("예약 삭제 시, 동일한 슬롯의 예약 대기가 있으면 예약으로 승격된다.")
    @Test
    void deleteReservationByIdTest_success() {
        //given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "이름", "설명", "thumbnailUrl");
        LocalDate date = LocalDate.of(2026, 5, 15);

        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        date,
                        time,
                        theme
                )));

        when(reservationWaitingRepository.findFirstByReservationDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.of(new ReservationWaiting(
                        1L,
                        "포비",
                        date,
                        time,
                        theme
                )));

        when(reservationRepository.deleteById(any()))
                .thenReturn(1);

        when(reservationWaitingRepository.deleteById(any()))
                .thenReturn(1);

        //when
        reservationService.deleteReservationById(1L);

        //then
        verify(reservationRepository).deleteById(1L);
        verify(reservationWaitingRepository).findFirstByReservationDateAndTimeIdAndThemeId(date, 1L, 1L);
        verify(reservationWaitingRepository).deleteById(1L);
        verify(reservationRepository).save(argThat(reservation ->
                reservation.getName().equals("포비")
                        && reservation.getDate().equals(date)
                        && reservation.getTime().equals(time)
                        && reservation.getTheme().equals(theme)
        ));
    }

    @DisplayName("id에 해당하는 예약이 없으면 예외가 발생한다.")
    @Test
    void deleteReservationByIdTest_reservation_not_found() {
        //given
        when(reservationRepository.findById(any()))
                .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> reservationService.deleteReservationById(1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("예약 삭제 시, 동일한 슬롯의 예약 대기가 없으면 예약만 삭제된다.")
    @Test
    void deleteReservationByIdTest_waiting_not_found() {
        //given
        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl")
                )));

        when(reservationWaitingRepository.findFirstByReservationDateAndTimeIdAndThemeId(any(), any(), any()))
                .thenReturn(Optional.empty());

        when(reservationRepository.deleteById(any()))
                .thenReturn(1);

        //when
        reservationService.deleteReservationById(1L);

        //then
        verify(reservationRepository).deleteById(1L);
        verify(reservationWaitingRepository, never()).deleteById(any());
        verify(reservationRepository, never()).save(any());
    }

    @DisplayName("현재 시간을 기준으로 예약의 유효성을 검증한다.")
    @Test
    void validateReservationNotExpiredTest() {
        //given
        when(reservationRepository.findById(any()))
                .thenReturn(Optional.of(new Reservation(
                        1L,
                        "브라운",
                        LocalDate.of(2026, 5, 15),
                        new ReservationTime(1L, LocalTime.of(10, 0)),
                        new Theme(1L, "이름", "설명", "thumbnailUrl")
                )));

        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        //when & then
        assertThatCode(() -> reservationService.validateReservationNotExpired(1L))
                .doesNotThrowAnyException();;
    }
}
