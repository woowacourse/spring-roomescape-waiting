package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsPendingException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsThemeException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsTimeException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.reservation.ReservationPastTimeException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepositoryImpl;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.MineReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationRepositoryImpl;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepositoryImpl;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryImpl;

@DataJpaTest
@Sql(scripts = "classpath:/initialize_database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({
        MemberRepositoryImpl.class,
        ThemeRepositoryImpl.class,
        ReservationTimeRepositoryImpl.class,
        ReservationRepositoryImpl.class,
        ReservationService.class
})
public class ReservationServiceTest {

    @MockitoSpyBean
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepositoryImpl themeRepositoryFacade;
    private final MemberRepositoryImpl memberRepositoryFacade;

    @Autowired
    public ReservationServiceTest(
            final ReservationRepository reservationRepository,
            final ReservationService reservationService,

            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepositoryImpl themeRepositoryFacade,
            final MemberRepositoryImpl memberRepositoryFacade
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;

        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepositoryFacade = themeRepositoryFacade;
        this.memberRepositoryFacade = memberRepositoryFacade;

    }

    @Nested
    @DisplayName("예약 생성")
    class Create {

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when
            final ReservationResponse actual = reservationService.create(request, loginMember);

            // then
            assertSoftly(s -> {
                s.assertThat(actual.id()).isNotNull();
                s.assertThat(actual.theme().name()).isEqualTo("야당");
                s.assertThat(actual.theme().description()).isEqualTo("야당당");
                s.assertThat(actual.theme().thumbnail()).isEqualTo("123");
                s.assertThat(actual.date()).isEqualTo(LocalDate.of(2025, 12, 30));
                s.assertThat(actual.time().startAt()).isEqualTo(LocalTime.of(12, 40));
                s.assertThat(actual.member().name()).isEqualTo("boogie");
            });
        }

        @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create1() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final Theme theme = new Theme("야당", "야당당", "123");
            memberRepositoryFacade.save(member);
            themeRepositoryFacade.save(theme);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationNotExistsTimeException.class);
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");

            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationNotExistsMemberException.class);
        }

        @DisplayName("이미 해당 시간, 날짜, 테마에 PENDING 상태의 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationConflictException.class);
        }

        @DisplayName("과거 날짜로 예약하려면 예외가 발생한다.")
        @Test
        void create4() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final ReservationRequest request = new ReservationRequest(LocalDate.now().minusDays(1), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationPastDateException.class);
        }

        @DisplayName("오늘의 지나간 시간으로 예약하려고하면 예외가 발생한다.")
        @Test
        void create5() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.now().minusMinutes(1));
            final Theme theme = new Theme("야당", "야당당", "123");
            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final ReservationRequest request = new ReservationRequest(LocalDate.now(), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationPastTimeException.class);
        }
    }

    @Nested
    @DisplayName("admin을 위한 예약 생성")
    class CreateForAdmin {

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(22, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final AdminReservationRequest request = new AdminReservationRequest(
                    LocalDate.of(2025, 12, 30), 1L, 1L, 1L);

            // when
            final ReservationResponse actual = reservationService.createForAdmin(request);

            // then
            assertSoftly(s -> {
                s.assertThat(actual.id()).isNotNull();
                s.assertThat(actual.theme().name()).isEqualTo("야당");
                s.assertThat(actual.theme().description()).isEqualTo("야당당");
                s.assertThat(actual.theme().thumbnail()).isEqualTo("123");
                s.assertThat(actual.date()).isEqualTo(LocalDate.of(2025, 12, 30));
                s.assertThat(actual.time().startAt()).isEqualTo(LocalTime.of(22, 40));
                s.assertThat(actual.member().name()).isEqualTo("boogie");
            });
        }

        @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create1() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);

            final AdminReservationRequest request = new AdminReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L, 1L);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final Theme theme = new Theme("야당", "야당당", "123");
            memberRepositoryFacade.save(member);
            themeRepositoryFacade.save(theme);

            final AdminReservationRequest request = new AdminReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L, 1L);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationNotExistsTimeException.class);
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final AdminReservationRequest request = new AdminReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L, 1L);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationNotExistsMemberException.class);
        }

        @DisplayName("이미 해당 시간, 날짜, 테마에 PENDING 상태의 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final AdminReservationRequest request = new AdminReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L, 1L);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationConflictException.class);
        }

        @DisplayName("과거 날짜로 예약하려면 예외가 발생한다.")
        @Test
        void create4() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final AdminReservationRequest request = new AdminReservationRequest(LocalDate.now().minusDays(1), 1L, 1L,
                    1L);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationPastDateException.class);
        }

        @DisplayName("오늘의 지나간 시간으로 예약하려고하면 예외가 발생한다.")
        @Test
        void create5() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.now().minusMinutes(1));
            final Theme theme = new Theme("야당", "야당당", "123");
            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final AdminReservationRequest request = new AdminReservationRequest(LocalDate.now(), 1L, 1L, 1L);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
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
            // when
            final List<ReservationResponse> allReservation = reservationService.readAll();

            // then
            assertThat(allReservation).isEmpty();
        }

        @DisplayName("존재하는 reservation들을 모두 조회한다.")
        @Test
        void readAll2() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

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
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final Theme theme = new Theme("야당", "야당당", "123");

            memberRepositoryFacade.save(member);
            themeRepositoryFacade.save(theme);

            final AdminFilterReservationRequest request = new AdminFilterReservationRequest(
                    1L, 1L,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );

            // when
            final List<ReservationResponse> responses = reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).isEmpty();
        }

        @DisplayName("조건에 맞는 예약들을 모두 조회한다")
        @Test
        void readAllByMemberAndThemeAndDateRange2() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 11, 25, 12, 0);
            final ReservationDate minReservationDate = ReservationDate.of(LocalDate.of(2025, 12, 1),
                    currentDateTime.toLocalDate());
            final Reservation minReservation = Reservation.of(minReservationDate, member, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            final ReservationDate maxReservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation maxReservation = Reservation.of(maxReservationDate, member, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(minReservation);
            reservationRepository.save(maxReservation);

            final AdminFilterReservationRequest request = new AdminFilterReservationRequest(
                    1L, 1L,
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31)
            );

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
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");

            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            // when
            reservationService.deleteById(1L);

            // then
            then(reservationRepository).should().deleteById(1L);
        }

        @DisplayName("주어진 id에 해당하는 reservation이 없다면 예외가 발생한다.")
        @Test
        void delete2() {
            // given
            // when & then
            assertThatThrownBy(() -> {
                reservationService.deleteById(1L);
            }).isInstanceOf(ReservationNotFoundException.class);
        }


        @DisplayName("member의 예약을 모두 조회한다.")
        @Test
        void readAllReservation() {
            // given
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);
            final MineReservationResponse expected = new MineReservationResponse(
                    1L, "테마", LocalDate.of(2025, 12, 30),
                    LocalTime.of(12, 40), "예약", 0L
            );

            final Member member = new Member(loginMember.email(), "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("테마", "설명", "썸네일");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            // when
            final List<MineReservationResponse> actual = reservationService.readAllMine(loginMember);

            // then
            assertThat(actual)
                    .contains(expected)
                    .hasSize(1);
        }

        @DisplayName("이메일의 member가 존재하지 않는다면, 예외가 발생한다.")
        @Test
        void readAllReservation1() {
            // given
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.readAllMine(loginMember);
            }).isInstanceOf(ReservationNotExistsMemberException.class);
        }
    }

    @Nested
    @DisplayName("reservation waiting 생성")
    class createWaiting {

        @DisplayName("정상 생성 테스트")
        @Test
        void create1() {
            // given
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());

            final Member anotherMember = new Member("xxxx", "pass", "아서", MemberRole.MEMBER);
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);

            final Reservation reservation = Reservation.of(reservationDate, anotherMember, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            memberRepositoryFacade.save(anotherMember);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when
            final ReservationResponse actual = reservationService.createWaiting(request, loginMember);

            // then
            assertSoftly(s -> {
                s.assertThat(actual.id()).isNotNull();
                s.assertThat(actual.theme().name()).isEqualTo("야당");
                s.assertThat(actual.theme().description()).isEqualTo("야당당");
                s.assertThat(actual.theme().thumbnail()).isEqualTo("123");
                s.assertThat(actual.date()).isEqualTo(LocalDate.of(2025, 12, 30));
                s.assertThat(actual.time().startAt()).isEqualTo(LocalTime.of(12, 40));
                s.assertThat(actual.member().name()).isEqualTo("boogie");
            });
        }

        @DisplayName("member가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final Member anotherMember = new Member("xxxx", "pass", "아서", MemberRole.MEMBER);
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, anotherMember, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            reservationTimeRepository.save(reservationTime);
            memberRepositoryFacade.save(anotherMember);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createWaiting(request, loginMember);
            }).isInstanceOf(ReservationNotExistsMemberException.class);

        }

        @DisplayName("reservation time이 존재하지 않는다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final Theme theme = new Theme("야당", "야당당", "123");
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 41));
            final Member anotherMember = new Member("xxxx", "pass", "아서", MemberRole.MEMBER);
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, anotherMember, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            reservationTimeRepository.save(reservationTime);
            memberRepositoryFacade.save(member);
            memberRepositoryFacade.save(anotherMember);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 2L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createWaiting(request, loginMember);
            }).isInstanceOf(ReservationNotExistsTimeException.class);

        }

        @DisplayName("theme가 존재하지 않는다면 예외가 발생한다.")
        @Test
        void create4() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final Member anotherMember = new Member("xxxx", "pass", "아서", MemberRole.MEMBER);
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, anotherMember, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            themeRepositoryFacade.save(theme);
            memberRepositoryFacade.save(member);
            memberRepositoryFacade.save(anotherMember);
            reservationTimeRepository.save(reservationTime);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 2L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createWaiting(request, loginMember);
            }).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("이미 해당 시간, 날짜, 테마에 pending 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create5() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when
            assertThatThrownBy(() -> {
                reservationService.createWaiting(request, loginMember);
            }).isInstanceOf(ReservationConflictException.class);
        }

        @DisplayName("이미 해당 시간, 날짜, 테마에 pending 상태의 예약이 존재하지 않는다면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when
            assertThatThrownBy(() -> {
                reservationService.createWaiting(request, loginMember);
            }).isInstanceOf(ReservationNotExistsPendingException.class);
        }

        @DisplayName("과거 날짜로 예약하면, 예외가 발생한다.")
        @Test
        void create7() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");

            final Member anotherMember = new Member("xxxx", "pass", "아서", MemberRole.MEMBER);
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, anotherMember, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            memberRepositoryFacade.save(anotherMember);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2024, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when
            assertThatThrownBy(() -> {
                reservationService.createWaiting(request, loginMember);
            }).isInstanceOf(ReservationPastDateException.class);
        }

        @DisplayName("오늘 날짜에 지난 시간으로 예약하면, 예외가 발생한다.")
        @Test
        void create8() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.now().minusMinutes(1));
            final Theme theme = new Theme("야당", "야당당", "123");

            final Member anotherMember = new Member("xxxx", "pass", "아서", MemberRole.MEMBER);
            final LocalDateTime currentDateTime = LocalDateTime.now().minusMinutes(1);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.now(),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, anotherMember, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime.minusMinutes(1));

            memberRepositoryFacade.save(member);
            memberRepositoryFacade.save(anotherMember);

            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.now(), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when
            assertThatThrownBy(() -> {
                reservationService.createWaiting(request, loginMember);
            }).isInstanceOf(ReservationPastTimeException.class);
        }

        @DisplayName("오늘 날짜에 미래 시간으로 예약하면 정상적으로 생성된다.")
        @Test
        void create9() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.now().plusMinutes(1));
            final Theme theme = new Theme("야당", "야당당", "123");

            final Member anotherMember = new Member("xxxx", "pass", "아서", MemberRole.MEMBER);
            final LocalDateTime currentDateTime = LocalDateTime.of(2024, 12, 30, 12, 30);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, anotherMember, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);

            memberRepositoryFacade.save(member);
            memberRepositoryFacade.save(anotherMember);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            final ReservationRequest request = new ReservationRequest(LocalDate.of(2025, 12, 30), 1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "email", MemberRole.MEMBER);

            // when
            final ReservationResponse actual = reservationService.createWaiting(request, loginMember);

            // then
            assertSoftly(s -> {
                s.assertThat(actual.id()).isNotNull();
                s.assertThat(actual.theme().name()).isEqualTo("야당");
                s.assertThat(actual.theme().description()).isEqualTo("야당당");
                s.assertThat(actual.theme().thumbnail()).isEqualTo("123");
                s.assertThat(actual.date()).isEqualTo(LocalDate.of(2025, 12, 30));
                s.assertThat(actual.time().startAt()).isEqualTo(reservationTime.getStartAt());
                s.assertThat(actual.member().name()).isEqualTo("boogie");
            });

        }

        @DisplayName("pending 상태의 예약을 삭제하면 대기중인 다음 순번의 예약이 pending 상태로 변경된다.")
        @Test
        void create10() {
            // given
            final Member pendingMember = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final Member waitingMember = new Member("email1", "pass", "boogie", MemberRole.MEMBER);
            final Member waitingMember1 = new Member("email2", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.now().plusMinutes(1));
            final Theme theme = new Theme("야당", "야당당", "123");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation pendingReservation = Reservation.of(reservationDate, pendingMember, reservationTime, theme,
                    ReservationStatus.PENDING, currentDateTime);
            final Reservation waitingReservation = Reservation.of(reservationDate, waitingMember, reservationTime,
                    theme,
                    ReservationStatus.WAITING, currentDateTime);
            final Reservation waitingReservation1 = Reservation.of(reservationDate, waitingMember1, reservationTime,
                    theme,
                    ReservationStatus.WAITING, currentDateTime);

            memberRepositoryFacade.save(pendingMember);
            memberRepositoryFacade.save(waitingMember);
            memberRepositoryFacade.save(waitingMember1);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(pendingReservation);
            reservationRepository.save(waitingReservation);
            reservationRepository.save(waitingReservation1);

            // when
            reservationService.deleteById(1L);

            // then
            final Reservation actual1 = reservationRepository.findById(2L).get();
            assertThat(actual1.getReservationStatus()).isEqualTo(ReservationStatus.PENDING);

            final Reservation actual2 = reservationRepository.findById(3L).get();
            assertThat(actual2.getReservationStatus()).isEqualTo(ReservationStatus.WAITING);


        }

    }

    @Nested
    @DisplayName("존재하는 예약 대기를 모두 조회한다.")
    class ReadAllWaiting {

        @DisplayName("예약 대기를 모두 조회한다.")
        @Test
        void readAllWaitingByMember() {
            // given
            final Member member = new Member("email", "pass", "boogie", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Theme theme = new Theme("야당", "야당당", "123");
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, reservationTime, theme,
                    ReservationStatus.WAITING, currentDateTime);

            memberRepositoryFacade.save(member);
            reservationTimeRepository.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            // when
            final List<ReservationResponse> actual = reservationService.readAllWaiting();

            // then
            assertThat(actual).hasSize(1);
        }

        @DisplayName("예약 대기가 비어 있다면 빈 컬렉션을 응답한다.")
        @Test
        void readAllWaitingByMember1() {
            // given
            // when
            final List<ReservationResponse> actual = reservationService.readAllWaiting();

            // then
            assertThat(actual).isEmpty();
        }
    }
}
