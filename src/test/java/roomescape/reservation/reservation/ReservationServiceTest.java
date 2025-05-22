package roomescape.reservation.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.member.MemberRole;
import roomescape.reservation.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.reservation.dto.AdminReservationRequest;
import roomescape.reservation.reservation.dto.ReservationRequest;
import roomescape.reservation.reservation.dto.ReservationResponse;
import roomescape.reservationtime.ReservationTime;
import roomescape.schedule.Schedule;
import roomescape.schedule.ScheduleRepository;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static roomescape.util.TestFactory.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    private ReservationService reservationService;
    private ReservationRepository reservationRepository;
    private ScheduleRepository scheduleRepository;
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        reservationRepository = mock(ReservationRepository.class);
        scheduleRepository = mock(ScheduleRepository.class);
        memberRepository = mock(MemberRepository.class);
        reservationService = new ReservationService(
                reservationRepository,
                scheduleRepository,
                memberRepository
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
            given(scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(REQUEST.date(), schedule.getReservationTime().getId(), schedule.getTheme().getId()))
                    .willReturn(SCHEDULE);
            given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsBySchedule(schedule))
                    .willReturn(false);
            given(reservationRepository.save(
                    new Reservation(MEMBER.get(), schedule)))
                    .willReturn(RESERVATION);

            // when
            final ReservationResponse response = reservationService.create(REQUEST, LOGIN_MEMBER);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(RESERVATION));
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            given(scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(REQUEST.date(), SCHEDULE.get().getReservationTime().getId(), SCHEDULE.get().getTheme().getId()))
                    .willReturn(SCHEDULE);
            given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(REQUEST, LOGIN_MEMBER);
            }).isInstanceOf(ReservationNotExistsMemberException.class);
        }

        @DisplayName("이미 해당 시간, 날짜에 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            given(scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(REQUEST.date(), SCHEDULE.get().getReservationTime().getId(), SCHEDULE.get().getTheme().getId()))
                    .willReturn(SCHEDULE);
            given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsBySchedule(SCHEDULE.get()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(REQUEST, LOGIN_MEMBER);
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
            given(scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(REQUEST.date(), SCHEDULE.get().getReservationTime().getId(), SCHEDULE.get().getTheme().getId()))
                    .willReturn(SCHEDULE);
            given(memberRepository.findById(REQUEST.memberId()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsBySchedule(SCHEDULE.get()))
                    .willReturn(false);
            given(reservationRepository.save(
                    new Reservation(MEMBER.get(), SCHEDULE.get())))
                    .willReturn(RESERVATION);

            // when
            final ReservationResponse response = reservationService.createForAdmin(REQUEST);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(RESERVATION));
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            given(scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(REQUEST.date(), SCHEDULE.get().getReservationTime().getId(), SCHEDULE.get().getTheme().getId()))
                    .willReturn(SCHEDULE);
            given(memberRepository.findById(REQUEST.memberId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(REQUEST);
            }).isInstanceOf(ReservationNotExistsMemberException.class);
        }

        @DisplayName("이미 해당 시간, 날짜, 테마에 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            given(scheduleRepository.findByDateAndReservationTime_IdAndTheme_Id(REQUEST.date(), SCHEDULE.get().getReservationTime().getId(), SCHEDULE.get().getTheme().getId()))
                    .willReturn(SCHEDULE);
            given(memberRepository.findById(MEMBER.get().getId()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsBySchedule(SCHEDULE.get()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(REQUEST);
            }).isInstanceOf(ReservationConflictException.class);
        }
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
