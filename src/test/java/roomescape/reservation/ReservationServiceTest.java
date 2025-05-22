package roomescape.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.*;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.member.MemberRole;
import roomescape.reservation.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.reservation.dto.AdminReservationRequest;
import roomescape.reservation.reservation.dto.ReservationRequest;
import roomescape.reservation.reservation.dto.ReservationResponse;
import roomescape.reservation.reservation.Reservation;
import roomescape.reservation.reservation.ReservationRepository;
import roomescape.reservation.reservation.ReservationService;
import roomescape.reservation.reservation.ReservationStatus;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;

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
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        reservationRepository = mock(ReservationRepository.class);
        reservationTimeRepository = mock(ReservationTimeRepository.class);
        themeRepository = mock(ThemeRepository.class);
        memberRepository = mock(MemberRepository.class);
        reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository
        );
    }

    @Nested
    @DisplayName("예약 생성")
    class Create {

        private ReservationRequest REQUEST;
        private LoginMember LOGIN_MEMBER;
        private Optional<ReservationTime> RESERVATION_TIME;
        private Optional<Theme> THEME;
        private Optional<Member> MEMBER;
        private Reservation RESERVATION;

        @BeforeEach
        void setUp() {
            REQUEST = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
            LOGIN_MEMBER = new LoginMember("boogie", "asd@email.com", MemberRole.MEMBER);
            RESERVATION_TIME = Optional.of(reservationTimeWithId(REQUEST.timeId(), new ReservationTime(LocalTime.of(12, 40))));
            THEME = Optional.of(themeWithId(REQUEST.themeId(), new Theme("야당", "야당당", "123")));
            MEMBER = Optional.of(memberWithId(1L, new Member(LOGIN_MEMBER.email(), "password", "boogie", MemberRole.MEMBER)));
            RESERVATION = reservationWithId(1L, new Reservation(
                    REQUEST.date(),
                    MEMBER.get(),
                    RESERVATION_TIME.get(),
                    THEME.get(),
                    ReservationStatus.CONFIRMED
            ));
        }

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
            given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    RESERVATION_TIME.get(), REQUEST.date(), THEME.get()))
                    .willReturn(false);
            given(reservationRepository.save(
                    new Reservation(REQUEST.date(), MEMBER.get(), RESERVATION_TIME.get(), THEME.get(), ReservationStatus.CONFIRMED)))
                    .willReturn(RESERVATION);

            // when
            final ReservationResponse response = reservationService.create(REQUEST, LOGIN_MEMBER);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(RESERVATION));
        }

        @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
        @Test
        void create1() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(REQUEST, LOGIN_MEMBER);
            }).isInstanceOf(ReservationNotExistsTimeException.class);
        }

        @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.create(REQUEST, LOGIN_MEMBER)).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
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
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
            given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    RESERVATION_TIME.get(), REQUEST.date(), THEME.get()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(REQUEST, LOGIN_MEMBER);
            }).isInstanceOf(ReservationConflictException.class);
        }

        @DisplayName("과거 날짜로 예약하려면 예외가 발생한다.")
        @Test
        void create4() {
            // given
            ReservationRequest requestOfPast = new ReservationRequest(LocalDate.now().plusDays(-1), 1L, 1L);

            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
            given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    RESERVATION_TIME.get(), requestOfPast.date(), THEME.get()))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(requestOfPast, LOGIN_MEMBER);
            }).isInstanceOf(ReservationPastDateException.class);
        }

        @DisplayName("오늘의 지나간 시간으로 예약하려고하면 예외가 발생한다.")
        @Test
        void create5() {
            // given
            final ReservationRequest request = new ReservationRequest(
                    LocalDate.now(), 1L, 1L);

            given(reservationTimeRepository.findById(request.timeId()))
                    .willReturn(Optional.of(reservationTimeWithId(request.timeId(), new ReservationTime(LocalTime.now().minusHours(1)))));
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
            given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    RESERVATION_TIME.get(), request.date(), THEME.get()))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, LOGIN_MEMBER);
            }).isInstanceOf(ReservationPastTimeException.class);
        }
    }

    @Nested
    @DisplayName("admin을 위한 예약 생성")
    class CreateForAdmin {

        private AdminReservationRequest REQUEST;
        private Optional<ReservationTime> RESERVATION_TIME;
        private Optional<Theme> THEME;
        private Optional<Member> MEMBER;
        private Reservation RESERVATION;

        @BeforeEach
        void setUp() {
            REQUEST = new AdminReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L);
            RESERVATION_TIME = Optional.of(reservationTimeWithId(REQUEST.timeId(), new ReservationTime(LocalTime.of(12, 40))));
            THEME = Optional.of(themeWithId(REQUEST.themeId(), new Theme("야당", "야당당", "123")));
            MEMBER = Optional.of(memberWithId(REQUEST.memberId(), new Member("asd@naver.com", "password", "boogie", MemberRole.MEMBER)));
            RESERVATION = reservationWithId(1L, new Reservation(
                    REQUEST.date(),
                    MEMBER.get(),
                    RESERVATION_TIME.get(),
                    THEME.get(),
                    ReservationStatus.CONFIRMED
            ));
        }

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
            given(memberRepository.findById(MEMBER.get().getId()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    RESERVATION_TIME.get(), REQUEST.date(), THEME.get()))
                    .willReturn(false);
            given(reservationRepository.save(
                    new Reservation(REQUEST.date(), MEMBER.get(), RESERVATION_TIME.get(), THEME.get(), ReservationStatus.CONFIRMED)))
                    .willReturn(RESERVATION);

            // when
            final ReservationResponse response = reservationService.createForAdmin(REQUEST);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(RESERVATION));
        }

        @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
        @Test
        void create1() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(REQUEST);
            }).isInstanceOf(ReservationNotExistsTimeException.class);
        }

        @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(REQUEST);
            }).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
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
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
            given(memberRepository.findById(MEMBER.get().getId()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    RESERVATION_TIME.get(), REQUEST.date(), THEME.get()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(REQUEST);
            }).isInstanceOf(ReservationConflictException.class);
        }

        @DisplayName("과거 날짜로 예약하려면 예외가 발생한다.")
        @Test
        void create4() {
            // given
            AdminReservationRequest requestOfPast = new AdminReservationRequest(
                    LocalDate.now().plusDays(-1), 1L, 1L, 1L);

            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(RESERVATION_TIME);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME);
            given(memberRepository.findById(MEMBER.get().getId()))
                    .willReturn(MEMBER);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    RESERVATION_TIME.get(), requestOfPast.date(), THEME.get()))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(requestOfPast);
            }).isInstanceOf(ReservationPastDateException.class);
        }
//
//        @DisplayName("오늘의 지나간 시간으로 예약하려고하면 예외가 발생한다.")
//        @Test
//        void create5() {
//            // given
//            final AdminReservationRequest requestOfPast = new AdminReservationRequest(
//                    LocalDate.now(), 1L, 1L, 1L);
//
//            given(reservationTimeRepository.findById(requestOfPast.timeId()))
//                    .willReturn(RESERVATION_TIME);
//            given(themeRepository.findById(requestOfPast.themeId()))
//                    .willReturn(THEME);
//            given(memberRepository.findById(MEMBER.get().getId()))
//                    .willReturn(MEMBER);
//            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
//                    RESERVATION_TIME.get(), requestOfPast.date(), THEME.get()))
//                    .willReturn(false);
//
//            // when & then
//            assertThatThrownBy(() -> {
//                reservationService.createForAdmin(requestOfPast);
//            }).isInstanceOf(ReservationPastTimeException.class);
//        }
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
            Reservation reservation = new Reservation(
                    LocalDate.of(2024, 1, 1),
                    memberWithId(1L, new Member("boogie", "password", "boogie", MemberRole.MEMBER)),
                    reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40))),
                    themeWithId(1L, new Theme("야당", "야당당", "123")),
                    ReservationStatus.CONFIRMED
            );
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
            Member member = memberWithId(request.memberId(), new Member("boogie", "password", "boogie", MemberRole.MEMBER));
            Theme theme = themeWithId(request.themeId(), new Theme("야당", "야당당", "123"));
            given(memberRepository.findById(request.memberId()))
                    .willReturn(Optional.of(member));
            given(themeRepository.findById(request.themeId()))
                    .willReturn(Optional.of(theme));
            given(reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, request.from(), request.to())).willReturn(List.of());

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
            given(memberRepository.findById(request.memberId()))
                    .willReturn(Optional.of(member));
            given(themeRepository.findById(request.themeId()))
                    .willReturn(Optional.of(theme));
            given(reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, request.from(), request.to())).willReturn(List.of(
                    reservationWithId(1L, new Reservation(LocalDate.of(2024, 6, 15), member, reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40))), theme, ReservationStatus.CONFIRMED)),
                    reservationWithId(2L, new Reservation(LocalDate.of(2024, 7, 20), member, reservationTimeWithId(1L, new ReservationTime(LocalTime.of(12, 40))), theme, ReservationStatus.CONFIRMED))
            ));

            // when
            final List<ReservationResponse> responses =
                    reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).hasSize(2);
        }
    }
}
