package roomescape.reservation.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.booking.reservation.Reservation;
import roomescape.booking.reservation.ReservationRepository;
import roomescape.booking.reservation.ReservationService;
import roomescape.booking.reservation.dto.AdminFilterReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;
import roomescape.booking.schedule.Schedule;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    private ReservationService reservationService;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setup() {
        reservationRepository = mock(ReservationRepository.class);
        reservationService = new ReservationService(reservationRepository);
    }

    @Nested
    @DisplayName("예약 모두 조회")
    class ReadAll {

        @DisplayName("reservation이 없다면 빈 컬렉션을 조회한다.")
        @Test
        void readAll1() {
            // given
            given(reservationRepository.findAll())
                    .willReturn(List.of());

            // when
            final List<ReservationResponse> allReservation = reservationService.readAll();

            // then
            assertThat(allReservation).hasSize(0);
        }

        @DisplayName("존재하는 reservation들을 모두 조회한다.")
        @Test
        void readAll2() {
            // given
            LocalDate date = LocalDate.of(2024, 1, 1);
            ReservationTime reservationTime = reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40)));
            Theme theme = themeWithId(1L, new Theme("야당", "야당당", "123"));
            Schedule schedule = new Schedule(date, reservationTime, theme);
            Member member = memberWithId(1L, new Member("boogie", "password", "boogie", MemberRole.MEMBER));

            Reservation reservation = new Reservation(member, schedule);
            given(reservationRepository.findAll())
                    .willReturn(List.of(reservationWithId(1L, reservation)));

            // when
            final List<ReservationResponse> actual = reservationService.readAll();

            // then
            assertThat(actual).hasSize(1);
        }

    }

    @Nested
    @DisplayName("예약 멤버 id, 테마 id, 날짜 범위 기준 조회")
    class ReadAllByMemberAndThemeAndDateRange {

        @DisplayName("조건에 맞는 예약이 없다면 빈 컬렉션을 반환한다")
        @Test
        void readAllByMemberAndThemeAndDateRange1() {
            // given
            final AdminFilterReservationRequest request = new AdminFilterReservationRequest(
                    1L, 1L,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );

            given(reservationRepository.findAllByMember_IdAndSchedule_Theme_IdAndSchedule_DateBetween(request.memberId(), request.themeId(), request.from(), request.to())).willReturn(List.of());

            // when
            final List<ReservationResponse> responses = reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).isEmpty();
        }

        @DisplayName("조건에 맞는 예약들을 모두 조회한다")
        @Test
        void readAllByMemberAndThemeAndDateRange2() {
            // given
            final AdminFilterReservationRequest request = new AdminFilterReservationRequest(
                    1L, 1L,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );
            Member member = memberWithId(request.memberId(), new Member("boogie", "password", "boogie", MemberRole.MEMBER));
            Theme theme = themeWithId(request.themeId(), new Theme("야당", "야당당", "123"));
            ReservationTime reservationTime = reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40)));
            Schedule schedule1 = new Schedule(LocalDate.of(2024, 6, 13), reservationTime, theme);
            Schedule schedule2 = new Schedule(LocalDate.of(2024, 6, 14), reservationTime, theme);
            given(reservationRepository.findAllByMember_IdAndSchedule_Theme_IdAndSchedule_DateBetween(request.memberId(), request.themeId(), request.from(), request.to()))
                    .willReturn(List.of(
                            reservationWithId(1L, new Reservation(member, schedule1)),
                            reservationWithId(1L, new Reservation(member, schedule2))
                    ));

            // when
            final List<ReservationResponse> responses =
                    reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).hasSize(2);
        }
    }
}
