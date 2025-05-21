package roomescape.reservationtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeConflictException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeUsedException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.reservation.ReservationStatus;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.reservationTimeWithId;
import static roomescape.util.TestFactory.themeWithId;

@ExtendWith(MockitoExtension.class)
public class ReservationTimeServiceTest {

    private ReservationTimeService reservationTimeService;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = mock(ReservationTimeRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        themeRepository = mock(ThemeRepository.class);
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository,
                themeRepository);
    }

    @Nested
    @DisplayName("예약 시간 생성")
    class Create {

        @DisplayName("TimeRequest를 저장하고, 저장된 TimeResponse를 반환한다.")
        @Test
        void createTime1() {
            // given
            final LocalTime startAt = LocalTime.of(12, 40);
            final ReservationTimeRequest request = new ReservationTimeRequest(startAt);
            given(reservationTimeRepository.existsByStartAt(startAt))
                    .willReturn(false);
            final ReservationTime reservationTime = reservationTimeWithId(1L, new ReservationTime(startAt));
            given(reservationTimeRepository.save(new ReservationTime(startAt)))
                    .willReturn(reservationTime);

            // when
            final ReservationTimeResponse actual = reservationTimeService.create(request);

            // then
            assertThat(actual).isEqualTo(ReservationTimeResponse.from(reservationTime));
        }

        @DisplayName("이미 존재하는 시간이라면, 예외가 발생한다.")
        @Test
        void createTime2() {
            // given
            final LocalTime startAt = LocalTime.of(12, 40);
            final ReservationTimeRequest request = new ReservationTimeRequest(startAt);
            given(reservationTimeRepository.existsByStartAt(startAt))
                    .willReturn(true);

            // when
            assertThatThrownBy(() -> {
                reservationTimeService.create(request);
            }).isInstanceOf(ReservationTimeConflictException.class);
        }
    }

    @Nested
    @DisplayName("예약 시간 모두 조회")
    class findAll {

        @DisplayName("저장된 모든 TimeResponse를 반환한다.")
        @Test
        void findAllTime1() {
            // given
            given(reservationTimeRepository.findAll())
                    .willReturn(List.of(reservationTimeWithId(1L,
                            new ReservationTime(LocalTime.of(12, 40)))
                    ));

            // when
            final List<ReservationTimeResponse> actual = reservationTimeService.findAll();

            // then
            assertThat(actual)
                    .hasSize(1)
                    .contains(new ReservationTimeResponse(1L, LocalTime.of(12, 40)));
        }

        @DisplayName("저장된 TimeResponse이 없다면 빈 컬렉션을 반환한다.")
        @Test
        void findAllTime2() {
            // given
            given(reservationTimeRepository.findAll())
                    .willReturn(List.of());

            // when
            final List<ReservationTimeResponse> actual = reservationTimeService.findAll();

            // then
            assertThat(actual).hasSize(0);
        }
    }

    @Nested
    @DisplayName("alreadyBooked와 함께 모든 time 반환")
    class FindAllAvailableTimes {

        @DisplayName("존재하는 모든 시간을 반환한다.")
        @Test
        void findAllAvailableTimes() {
            // given
            final Long dummyThemeId = 1L;
            final Theme theme = themeWithId(dummyThemeId, new Theme("메이", "테마", "asd"));
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);
            given(reservationTimeRepository.findAll())
                    .willReturn(List.of(
                            reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 0))),
                            reservationTimeWithId(2L, new ReservationTime(LocalTime.of(13, 0))),
                            reservationTimeWithId(3L, new ReservationTime(LocalTime.of(14, 0)))
                    ));
            given(themeRepository.findById(dummyThemeId))
                    .willReturn(Optional.of(theme));
            given(reservationRepository.findAllByThemeAndDate(theme, targetDate))
                    .willReturn(List.of());

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailableTimes(
                    dummyThemeId, targetDate);

            // then
            assertThat(allAvailableTimes).hasSize(3);
        }

        @DisplayName("이미 예약된 시간은 alreadyBooked가 true로 반환된다.")
        @Test
        void findAllAvailableTimes1() {
            // given
            final Long dummyThemeId = 1L;
            final Theme theme = themeWithId(dummyThemeId, new Theme("메이", "테마", "asd"));
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);
            final ReservationTime savedTime = reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 0)));
            given(reservationTimeRepository.findAll())
                    .willReturn(List.of(
                            savedTime
                    ));
            given(themeRepository.findById(dummyThemeId))
                    .willReturn(Optional.of(theme));
            given(reservationRepository.findAllByThemeAndDate(theme, targetDate))
                    .willReturn(List.of(
                            new Reservation(null, null, savedTime, theme, ReservationStatus.CONFIRMED))
                    );

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailableTimes(
                    dummyThemeId, targetDate);

            // then
            assertThat(allAvailableTimes.getFirst().alreadyBooked()).isTrue();
        }

        @DisplayName("예약되지 않은 시간은 alreadyBooked가 false로 반환된다.")
        @Test
        void findAllAvailableTimes2() {
            // given
            final Long dummyThemeId = 1L;
            final Theme theme = themeWithId(dummyThemeId, new Theme("메이", "테마", "asd"));
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);
            given(reservationTimeRepository.findAll())
                    .willReturn(List.of(reservationTimeWithId(1L,
                            new ReservationTime(LocalTime.of(12, 0)))
                    ));
            given(themeRepository.findById(dummyThemeId))
                    .willReturn(Optional.of(theme));
            given(reservationRepository.findAllByThemeAndDate(theme, targetDate))
                    .willReturn(List.of());
            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailableTimes(
                    dummyThemeId, targetDate);

            // then
            assertThat(allAvailableTimes.getFirst().alreadyBooked()).isFalse();
        }

    }


    @Nested
    @DisplayName("예약 시간 삭제")
    class Delete {

        @DisplayName("id에 해당하는 time을 제거한다")
        @Test
        void deleteTimeById1() {
            // given
            final ReservationTime reservationTime = reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40)));
            given(reservationTimeRepository.findById(reservationTime.getId()))
                    .willReturn(Optional.of(reservationTime));
            given(reservationRepository.existsByReservationTime(reservationTime))
                    .willReturn(false);

            // when
            reservationTimeService.deleteById(1L);

            // then
            then(reservationTimeRepository).should().delete(reservationTime);
        }

        @DisplayName("id에 해당하는 time이 없다면 예외가 발생한다.")
        @Test
        void deleteTimeById2() {
            // given
            final ReservationTime reservationTime = reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40)));
            given(reservationTimeRepository.findById(reservationTime.getId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationTimeService.deleteById(1L);
            }).isInstanceOf(ReservationTimeNotFoundException.class);
        }

        @DisplayName("예약에서 시간을 사용중이라면 예외가 발생한다.")
        @Test
        void deleteTimeById3() {
            // given
            final ReservationTime reservationTime = reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40)));
            given(reservationTimeRepository.findById(reservationTime.getId()))
                    .willReturn(Optional.of(reservationTime));
            given(reservationRepository.existsByReservationTime(reservationTime))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationTimeService.deleteById(1L);
            }).isInstanceOf(ReservationTimeUsedException.class);
        }

    }

}
