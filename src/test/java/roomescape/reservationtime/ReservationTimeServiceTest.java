package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeConflictException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeUsedException;
import roomescape.reservation.FakeReservationRepository;
import roomescape.reservation.Reservation;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;

public class ReservationTimeServiceTest {

    private final ReservationTimeService reservationTimeService;
    private final FakeReservationTimeRepository fakeReservationTimeRepository;
    private final FakeReservationRepository fakeReservationRepository;

    public ReservationTimeServiceTest() {
        fakeReservationTimeRepository = new roomescape.reservationtime.FakeReservationTimeRepository();
        fakeReservationRepository = new FakeReservationRepository();
        reservationTimeService = new ReservationTimeService(fakeReservationTimeRepository, fakeReservationRepository);
    }

    @BeforeEach
    void setUp() {
        fakeReservationTimeRepository.clear();
        fakeReservationRepository.clear();
    }


    @Nested
    @DisplayName("예약 시간 생성")
    class Create {

        @DisplayName("TimeRequest를 저장하고, 저장된 TimeResponse를 반환한다.")
        @Test
        void createTime1() {
            // given
            final ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(12, 40));

            // when
            final ReservationTimeResponse actual = reservationTimeService.create(request);

            // then
            assertSoftly(s -> {
                s.assertThat(actual.id()).isEqualTo(1L);
                s.assertThat(actual.startAt()).isEqualTo(LocalTime.of(12, 40));
            });
        }

        @DisplayName("이미 존재하는 시간이라면, 예외가 발생한다.")
        @Test
        void createTime2() {
            // given
            final ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(12, 40));
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));

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
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));

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
            // given & when
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
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 5)));
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 10)));
            final Long dummyThemeId = 1L;
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);

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
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
            final Long dummyThemeId = 1L;
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);
            fakeReservationRepository.save(new Reservation(targetDate), 1L, dummyThemeId, 1L);

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailableTimes(dummyThemeId, targetDate);

            // then
            assertThat(allAvailableTimes.getFirst().alreadyBooked()).isTrue();
        }

        @DisplayName("예약되지 않은 시간은 alreadyBooked가 false로 반환된다.")
        @Test
        void findAllAvailableTimes2() {
            // given
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
            final Long dummyThemeId = 1L;
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailableTimes(dummyThemeId, targetDate);

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
            fakeReservationTimeRepository.save(new ReservationTime(null, LocalTime.of(12, 40)));

            // when
            reservationTimeService.deleteById(1L);

            // then
            assertThat(fakeReservationTimeRepository.isInvokeDeleteId(1L)).isTrue();
        }

        @DisplayName("id에 해당하는 time이 없다면 예외가 발생한다.")
        @Test
        void deleteTimeById2() {
            // given & when & then
            assertThatThrownBy(() -> {
                reservationTimeService.deleteById(1L);
            }).isInstanceOf(ReservationTimeNotFoundException.class);
        }

        @DisplayName("예약에서 시간을 사용중이라면 예외가 발생한다.")
        @Test
        void deleteTimeById3() {
            // given
            fakeReservationRepository.save(new Reservation(
                    LocalDate.of(2026, 12, 01)), 1L, 1L, 1L
            );
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));

            // when & then
            assertThatThrownBy(() -> {
                reservationTimeService.deleteById(1L);
            }).isInstanceOf(ReservationTimeUsedException.class);
        }

    }

}
