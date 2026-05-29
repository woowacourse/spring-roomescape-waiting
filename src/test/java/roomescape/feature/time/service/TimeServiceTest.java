package roomescape.feature.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.feature.time.dto.command.TimeCreateCommand;
import roomescape.feature.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.feature.time.dto.response.TimeResponseDto;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.mapper.TimeMapper;
import roomescape.feature.time.repository.TimeRepository;
import roomescape.fixture.TimeFixture;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralParametersException;

@ExtendWith(MockitoExtension.class)
class TimeServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TimeRepository timeRepository;
    @Mock
    private ThemeRepository themeRepository;

    private TimeService timeService;

    @BeforeEach
    void setUp() {
        timeService = new TimeService(reservationRepository, timeRepository, themeRepository, new TimeMapper());
    }

    @Nested
    class 예약_시간_목록_조회 {

        @Test
        void 예약_시간이_없으면_빈_목록을_반환한다() {
            when(timeRepository.findAllByDeletedAtIsNull()).thenReturn(List.of());

            assertThat(timeService.getTimes()).isEmpty();
        }

        @Test
        void 활성_예약_시간_목록을_조회한다() {
            // given
            Time time1 = Time.reconstruct(1L, LocalTime.of(10, 0), null);
            Time time2 = Time.reconstruct(2L, LocalTime.of(15, 30), null);
            when(timeRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(time1, time2));

            // when
            List<TimeResponseDto> result = timeService.getTimes();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(TimeResponseDto::startAt)
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(15, 30));
        }
    }

    @Nested
    class 예약_시간_가용성_조회 {

        @Test
        void 예약된_시간은_available이_false이다() {
            // given
            LocalDate date = LocalDate.of(2026, 5, 10);
            Long themeId = 1L;
            Time reservedTime = Time.reconstruct(1L, LocalTime.of(10, 0), null);
            Time availableTime = Time.reconstruct(2L, LocalTime.of(11, 0), null);
            when(themeRepository.existsThemeByIdAndNotDeleted(themeId)).thenReturn(true);
            when(reservationRepository.findTimeIdsByDateAndThemeIdAndNotDeleted(date, themeId))
                .thenReturn(List.of(1L));
            when(timeRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(reservedTime, availableTime));

            // when
            List<TimeAvailabilityResponseDto> result = timeService.getTimeAvailabilities(date, themeId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).available()).isFalse();
            assertThat(result.get(1).available()).isTrue();
        }

        @Test
        void 존재하지_않는_themeId이면_파라미터_에러가_발생한다() {
            // given
            when(themeRepository.existsThemeByIdAndNotDeleted(999L)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> timeService.getTimeAvailabilities(LocalDate.of(2026, 5, 10), 999L))
                .isInstanceOfSatisfying(GeneralParametersException.class, ex -> {
                    assertThat(ex.getMessage()).isEqualTo("조회할 자원이 존재하지 않습니다.");
                    assertThat(ex.getParameterErrors())
                        .extracting(ParameterErrorResponseDto::parameter)
                        .containsExactly("themeId");
                });
        }
    }

    @Nested
    class 예약_시간_생성 {

        @Test
        void 예약_시간을_생성한다() {
            // given
            TimeCreateCommand command = new TimeCreateCommand(TimeFixture.VALID_10_00.getStartAt());
            Time saved = Time.reconstruct(1L, TimeFixture.VALID_10_00.getStartAt(), null);
            when(timeRepository.existsTimeByStartAtAndDeletedAtIsNull(TimeFixture.VALID_10_00.getStartAt()))
                .thenReturn(false);
            when(timeRepository.save(any(Time.class))).thenReturn(saved);

            // when
            TimeResponseDto result = timeService.saveTime(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.startAt()).isEqualTo(TimeFixture.VALID_10_00.getStartAt());
        }

        @Test
        void 이미_등록된_예약_시간이면_예외가_발생한다() {
            // given
            TimeCreateCommand command = new TimeCreateCommand(TimeFixture.VALID_10_00.getStartAt());
            when(timeRepository.existsTimeByStartAtAndDeletedAtIsNull(TimeFixture.VALID_10_00.getStartAt()))
                .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> timeService.saveTime(command))
                .isInstanceOf(GeneralException.class)
                .hasMessage("이미 등록된 예약 시간입니다.");
        }
    }

    @Nested
    class 예약_시간_삭제 {

        @Test
        void 예약_시간을_삭제한다() {
            when(timeRepository.existsTimeByIdAndDeletedAtIsNull(1L)).thenReturn(true);

            timeService.deleteTimeById(1L);
        }

        @Test
        void 존재하지_않는_예약_시간_ID이면_예외가_발생한다() {
            // given
            when(timeRepository.existsTimeByIdAndDeletedAtIsNull(999L)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> timeService.deleteTimeById(999L))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약 시간을 찾을 수 없습니다.");
        }
    }
}
