package roomescape.reservation.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoginMember;
import roomescape.booking.reservation.Reservation;
import roomescape.booking.reservation.ReservationCreateService;
import roomescape.booking.reservation.ReservationRepository;
import roomescape.booking.reservation.dto.AdminReservationRequest;
import roomescape.booking.reservation.dto.ReservationRequest;
import roomescape.booking.reservation.dto.ReservationResponse;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.schedule.ScheduleService;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.member.MemberService;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
public class ReservationCreateServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ScheduleService scheduleService;
    @Mock
    private MemberService memberService;
    @InjectMocks
    private ReservationCreateService reservationCreateService;

    @Nested
    @DisplayName("예약 생성")
    class Create {

        private ReservationRequest request;
        private LoginMember loginMember;
        private Member member;
        private Schedule schedule;
        private Reservation reservation;

        @BeforeEach
        void setUp() {
            request = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
            loginMember = new LoginMember("boogie", "asd@email.com", MemberRole.MEMBER);
            ReservationTime reservationTime = reservationTimeWithId(request.timeId(), new ReservationTime(LocalTime.of(12, 40)));
            Theme theme = themeWithId(request.themeId(), new Theme("야당", "야당당", "123"));
            schedule = new Schedule(request.date(), reservationTime, theme);
            member = memberWithId(1L, new Member(loginMember.email(), "password", "boogie", MemberRole.MEMBER));
            reservation = reservationWithId(1L, new Reservation(member, schedule));
        }

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            given(scheduleService.findByDateAndTimeIdAndThemeId(request.date(), schedule.getReservationTime().getId(), schedule.getTheme().getId()))
                    .willReturn(schedule);
            given(memberService.findByEmail(loginMember.email()))
                    .willReturn(member);
            given(reservationRepository.existsBySchedule(schedule))
                    .willReturn(false);
            given(reservationRepository.save(
                    new Reservation(member, schedule)))
                    .willReturn(reservation);

            // when
            final ReservationResponse response = reservationCreateService.create(request, loginMember);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(reservation));
        }

        @DisplayName("이미 해당 시간, 날짜에 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            given(scheduleService.findByDateAndTimeIdAndThemeId(request.date(), schedule.getReservationTime().getId(), schedule.getTheme().getId()))
                    .willReturn(schedule);
            given(reservationRepository.existsBySchedule(schedule))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationCreateService.create(request, loginMember);
            }).isInstanceOf(ReservationConflictException.class);
        }
    }

    @Nested
    @DisplayName("admin을 위한 예약 생성")
    class CreateForAdmin {

        private AdminReservationRequest request;
        private Schedule schedule;
        private Member member;
        private Reservation reservation;

        @BeforeEach
        void setUp() {
            request = new AdminReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L);
            ReservationTime reservationTime = reservationTimeWithId(request.timeId(), new ReservationTime(LocalTime.of(12, 40)));
            Theme theme = themeWithId(request.themeId(), new Theme("야당", "야당당", "123"));
            schedule = new Schedule(request.date(), reservationTime, theme);
            member = memberWithId(1L, new Member("user@example.com", "password", "boogie", MemberRole.MEMBER));
            reservation = reservationWithId(1L, new Reservation(member, schedule));
        }

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            given(scheduleService.findByDateAndTimeIdAndThemeId(request.date(), schedule.getReservationTime().getId(), schedule.getTheme().getId()))
                    .willReturn(schedule);
            given(memberService.findById(request.memberId()))
                    .willReturn(member);
            given(reservationRepository.existsBySchedule(schedule))
                    .willReturn(false);
            given(reservationRepository.save(
                    new Reservation(member, schedule)))
                    .willReturn(reservation);

            // when
            final ReservationResponse response = reservationCreateService.createForAdmin(request);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(reservation));
        }

        @DisplayName("이미 해당 시간, 날짜, 테마에 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            given(scheduleService.findByDateAndTimeIdAndThemeId(request.date(), schedule.getReservationTime().getId(), schedule.getTheme().getId()))
                    .willReturn(schedule);
            given(reservationRepository.existsBySchedule(schedule))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationCreateService.createForAdmin(request);
            }).isInstanceOf(ReservationConflictException.class);
        }
    }
}
