package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsThemeException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsTimeException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.reservation.ReservationPastTimeException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.member.MemberRole;
import roomescape.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    private final ReservationService reservationService;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;

    public ReservationServiceTest() {
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

        private static final ReservationRequest REQUEST =
                new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
        private static final LoginMember LOGIN_MEMBER =
                new LoginMember("boogie", "asd@email.com", MemberRole.MEMBER);
        private static final Optional<ReservationTime> TIME_BY_ID = Optional.of(
                new ReservationTime(REQUEST.timeId(), LocalTime.of(12, 40)));
        private static final Optional<Theme> THEME_BY_ID = Optional.of(
                new Theme(REQUEST.themeId(), "야당", "야당당", "123"));
        private static final Optional<Member> MEMBER_BY_EMAIL = Optional.of(
                new Member(1L, LOGIN_MEMBER.email(), "password", "boogie", MemberRole.MEMBER));
        private static final Reservation RESERVATION = new Reservation(
                1L,
                REQUEST.date(),
                MEMBER_BY_EMAIL.get(),
                TIME_BY_ID.get(),
                THEME_BY_ID.get()
        );

        @BeforeEach
        void setUp() {
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(TIME_BY_ID);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME_BY_ID);
            given(memberRepository.findByEmail(LOGIN_MEMBER.email()))
                    .willReturn(MEMBER_BY_EMAIL);
            given(reservationRepository.save(
                    new Reservation(REQUEST.date(), MEMBER_BY_EMAIL.get(), TIME_BY_ID.get(), THEME_BY_ID.get())))
                    .willReturn(RESERVATION);
            given(reservationRepository.existsById(RESERVATION.getId()))
                    .willReturn(true);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    TIME_BY_ID.get(), REQUEST.date(), THEME_BY_ID.get()))
                    .willReturn(false);
        }

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given

            // when
            final ReservationResponse response = reservationService.create(REQUEST, LOGIN_MEMBER);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(RESERVATION));
        }

        @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create1() {
            // given
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(REQUEST, LOGIN_MEMBER);
            }).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(REQUEST, LOGIN_MEMBER);
            }).isInstanceOf(ReservationNotExistsTimeException.class);
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
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
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    TIME_BY_ID.get(), REQUEST.date(), THEME_BY_ID.get()))
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
                    .willReturn(Optional.of(new ReservationTime(request.timeId(), LocalTime.now().minusHours(1))));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, LOGIN_MEMBER);
            }).isInstanceOf(ReservationPastTimeException.class);
        }
    }

    @Nested
    @DisplayName("admin을 위한 예약 생성")
    class CreateForAdmin {

        private static final AdminReservationRequest REQUEST =
                new AdminReservationRequest(LocalDate.now().plusDays(1), 1L, 1L, 1L);
        private static final Optional<ReservationTime> TIME_BY_ID = Optional.of(
                new ReservationTime(REQUEST.timeId(), LocalTime.of(12, 40)));
        private static final Optional<Theme> THEME_BY_ID = Optional.of(
                new Theme(REQUEST.themeId(), "야당", "야당당", "123"));
        private static final Optional<Member> MEMBER_BY_ID = Optional.of(
                new Member(REQUEST.memberId(), "asd@naver.com", "password", "boogie", MemberRole.MEMBER));
        private static final Reservation RESERVATION = new Reservation(
                1L,
                REQUEST.date(),
                MEMBER_BY_ID.get(),
                TIME_BY_ID.get(),
                THEME_BY_ID.get()
        );

        @BeforeEach
        void setUp() {
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(TIME_BY_ID);
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(THEME_BY_ID);
            given(reservationRepository.save(
                    new Reservation(REQUEST.date(), MEMBER_BY_ID.get(), TIME_BY_ID.get(), THEME_BY_ID.get())))
                    .willReturn(RESERVATION);
            given(memberRepository.findById(MEMBER_BY_ID.get().getId()))
                    .willReturn(MEMBER_BY_ID);
            given(reservationRepository.existsById(RESERVATION.getId()))
                    .willReturn(true);
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    TIME_BY_ID.get(), REQUEST.date(), THEME_BY_ID.get()))
                    .willReturn(false);
        }

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            // when
            final ReservationResponse response = reservationService.createForAdmin(REQUEST);

            // then
            assertThat(response).isEqualTo(ReservationResponse.from(RESERVATION));
        }

        @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create1() {
            // given
            given(themeRepository.findById(REQUEST.themeId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(REQUEST);
            }).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            given(reservationTimeRepository.findById(REQUEST.timeId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(REQUEST);
            }).isInstanceOf(ReservationNotExistsTimeException.class);
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
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
            given(reservationRepository.existsByReservationTimeAndDateAndTheme(
                    TIME_BY_ID.get(), REQUEST.date(), THEME_BY_ID.get()))
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

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(requestOfPast);
            }).isInstanceOf(ReservationPastDateException.class);
        }

        @DisplayName("오늘의 지나간 시간으로 예약하려고하면 예외가 발생한다.")
        @Test
        void create5() {
            // given
            final AdminReservationRequest requestOfPast = new AdminReservationRequest(
                    LocalDate.now(), 1L, 1L, 1L);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(requestOfPast);
            }).isInstanceOf(ReservationPastTimeException.class);
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
            given(reservationRepository.findAll())
                    .willReturn(List.of(new Reservation( 1L,
                            LocalDate.of(2024, 1, 1),
                            new Member(1L, "boogie", "password", "boogie", MemberRole.MEMBER),
                            new ReservationTime(1L, LocalTime.of(12, 40)),
                            new Theme(1L, "야당", "야당당", "123")
                    )));

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
            Member member = new Member(request.memberId(), "boogie", "password", "boogie", MemberRole.MEMBER);
            Theme theme = new Theme(request.themeId(), "야당", "야당당", "123");
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
            Member member = new Member(request.memberId(), "boogie", "password", "boogie", MemberRole.MEMBER);
            Theme theme = new Theme(request.themeId(), "야당", "야당당", "123");
            given(memberRepository.findById(request.memberId()))
                    .willReturn(Optional.of(member));
            given(themeRepository.findById(request.themeId()))
                    .willReturn(Optional.of(theme));
            given(reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, request.from(), request.to())).willReturn(List.of(
                    new Reservation(1L, LocalDate.of(2024, 6, 15), member, new ReservationTime(1L, LocalTime.of(12, 40)), theme),
                    new Reservation(2L, LocalDate.of(2024, 7, 20), member, new ReservationTime(1L, LocalTime.of(12, 40)), theme)
            ));

            // when
            final List<ReservationResponse> responses =
                    reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).hasSize(2);
        }
    }

    @Nested
    @DisplayName("예약 삭제")
    class Delete {

        @DisplayName("주어진 id에 해당하는 reservation 삭제한다.")
        @Test
        void delete1() {
            // given
            final Long id = 1L;
            given(reservationRepository.existsById(id))
                    .willReturn(true);

            // when
            reservationService.deleteById(id);

            // then
            then(reservationRepository).should().deleteById(id);
        }

        @DisplayName("주어진 id에 해당하는 reservation이 없다면 예외가 발생한다.")
        @Test
        void delete2() {
            // given
            final Long id = 1L;
            given(reservationRepository.existsById(id))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.deleteById(id);
            }).isInstanceOf(ReservationNotFoundException.class);
        }
    }
}
