package roomescape.reservation.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
public class ReservationCreateServiceTest {

    private ReservationCreateService reservationCreateService;
    private ReservationRepository reservationRepository;
    private ScheduleService scheduleService;
    private MemberService memberService;

    @BeforeEach
    void setup() {
        reservationRepository = mock(ReservationRepository.class);
        scheduleService = mock(ScheduleService.class);
        memberService = mock(MemberService.class);
        reservationCreateService = new ReservationCreateService(
                reservationRepository,
                scheduleService,
                memberService
        );
    }

    @Nested
    @DisplayName("예약 생성")
    class Create {

        private ReservationRequest REQUEST;
        private LoginMember LOGIN_MEMBER;
        private Optional<Member> MEMBER;
        private Optional<Schedule> SCHEDULE;
        private Reservation RESERVATION;

        @BeforeEach
        void setUp() {
            REQUEST = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
            LOGIN_MEMBER = new LoginMember("boogie", "asd@email.com", MemberRole.MEMBER);
            ReservationTime reservationTime = reservationTimeWithId(REQUEST.timeId(), new ReservationTime(LocalTime.of(12, 40)));
            Theme theme = themeWithId(REQUEST.themeId(), new Theme("야당", "야당당", "123"));
            SCHEDULE = Optional.of(new Schedule(REQUEST.date(), reservationTime, theme));
            MEMBER = Optional.of(memberWithId(1L, new Member(LOGIN_MEMBER.email(), "password", "boogie", MemberRole.MEMBER)));
            RESERVATION = reservationWithId(1L, new Reservation(MEMBER.get(), SCHEDULE.get()));
        }

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            Schedule schedule = SCHEDULE.get();
            given(scheduleService.findByDateAndTimeIdAndThemeId(REQUEST.date(), schedule.getReservationTime().getId(), schedule.getTheme().getId()))
                    .willReturn(schedule);
            given(memberService.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER.get());
            given(reservationRepository.existsBySchedule(schedule))
                    .willReturn(false);
            given(reservationRepository.save(
                    new Reservation(MEMBER.get(), schedule)))
                    .willReturn(RESERVATION);

            // when
            final ReservationResponse response = reservationCreateService.create(REQUEST, LOGIN_MEMBER);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(RESERVATION));
        }

        @DisplayName("이미 해당 시간, 날짜에 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            given(scheduleService.findByDateAndTimeIdAndThemeId(REQUEST.date(), SCHEDULE.get().getReservationTime().getId(), SCHEDULE.get().getTheme().getId()))
                    .willReturn(SCHEDULE.get());
            given(memberService.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER.get());
            given(reservationRepository.existsBySchedule(SCHEDULE.get()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationCreateService.create(REQUEST, LOGIN_MEMBER);
            }).isInstanceOf(ReservationConflictException.class);
        }
    }

    @Nested
    @DisplayName("admin을 위한 예약 생성")
    class CreateForAdmin {

        private AdminReservationRequest REQUEST;
        private Optional<Schedule> SCHEDULE;
        private Optional<Member> MEMBER;
        private Reservation RESERVATION;

        @BeforeEach
        void setUp() {
            REQUEST = new AdminReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L);
            ReservationTime reservationTime = reservationTimeWithId(REQUEST.timeId(), new ReservationTime(LocalTime.of(12, 40)));
            Theme theme = themeWithId(REQUEST.themeId(), new Theme("야당", "야당당", "123"));
            SCHEDULE = Optional.of(new Schedule(REQUEST.date(), reservationTime, theme));
            MEMBER = Optional.of(memberWithId(1L, new Member("user@example.com", "password", "boogie", MemberRole.MEMBER)));
            RESERVATION = reservationWithId(1L, new Reservation(MEMBER.get(), SCHEDULE.get()));
        }

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            given(scheduleService.findByDateAndTimeIdAndThemeId(REQUEST.date(), SCHEDULE.get().getReservationTime().getId(), SCHEDULE.get().getTheme().getId()))
                    .willReturn(SCHEDULE.get());
            given(memberService.findById(REQUEST.memberId()))
                    .willReturn(MEMBER.get());
            given(reservationRepository.existsBySchedule(SCHEDULE.get()))
                    .willReturn(false);
            given(reservationRepository.save(
                    new Reservation(MEMBER.get(), SCHEDULE.get())))
                    .willReturn(RESERVATION);

            // when
            final ReservationResponse response = reservationCreateService.createForAdmin(REQUEST);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(RESERVATION));
        }

        @DisplayName("이미 해당 시간, 날짜, 테마에 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            given(scheduleService.findByDateAndTimeIdAndThemeId(REQUEST.date(), SCHEDULE.get().getReservationTime().getId(), SCHEDULE.get().getTheme().getId()))
                    .willReturn(SCHEDULE.get());
            given(memberService.findById(MEMBER.get().getId()))
                    .willReturn(MEMBER.get());
            given(reservationRepository.existsBySchedule(SCHEDULE.get()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationCreateService.createForAdmin(REQUEST);
            }).isInstanceOf(ReservationConflictException.class);
        }
    }
}
