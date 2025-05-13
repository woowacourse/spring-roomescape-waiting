package roomescape.reservation;

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
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsThemeException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsTimeException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.reservation.ReservationPastTimeException;
import roomescape.member.FakeMemberRepository;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationtime.FakeReservationTimeRepository;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.FakeThemeRepository;
import roomescape.theme.Theme;

public class ReservationServiceTest {

    private final ReservationService reservationService;
    private final FakeReservationRepository fakeReservationRepository;
    private final FakeReservationTimeRepository fakeReservationTimeRepository;
    private final FakeThemeRepository fakeThemeRepository;
    private final FakeMemberRepository fakeMemberRepository;

    public ReservationServiceTest() {
        fakeReservationRepository = new FakeReservationRepository();
        fakeReservationTimeRepository = new FakeReservationTimeRepository();
        fakeThemeRepository = new FakeThemeRepository();
        fakeMemberRepository = new FakeMemberRepository();
        reservationService = new ReservationService(
                fakeReservationRepository,
                fakeReservationTimeRepository,
                fakeThemeRepository,
                fakeMemberRepository
        );
    }

    @BeforeEach
    void setUp() {
        fakeReservationRepository.clear();
        fakeReservationTimeRepository.clear();
        fakeThemeRepository.clear();
        fakeMemberRepository.clear();
    }

    @Nested
    @DisplayName("예약 생성")
    class Create {

        @DisplayName("reservation request를 생성하면 response 값을 반환한다.")
        @Test
        void create() {
            // given
            final ReservationRequest request = new ReservationRequest(
                    LocalDate.now().plusDays(1),
                    1L, 1L);
            final LoginMember loginMember = new LoginMember("boogie", "asd@email.com", MemberRole.MEMBER);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

            // when
            final ReservationResponse response = reservationService.create(request, loginMember);

            // then
            assertSoftly(s -> {
                s.assertThat(response.id()).isNotNull();
                s.assertThat(response.date()).isEqualTo(request.date());
            });
        }

        @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create1() {
            // given
            final ReservationRequest request = new ReservationRequest(
                    LocalDate.now().plusDays(1),
                    1L, 1L);
            final LoginMember loginMember = new LoginMember("로키", "asd@email.com", MemberRole.MEMBER);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            final ReservationRequest request = new ReservationRequest(
                    LocalDate.now().plusDays(1),
                    1L, 1L);
            final LoginMember loginMember = new LoginMember("로키", "asd@email.com", MemberRole.MEMBER);
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationNotExistsTimeException.class);
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            final ReservationRequest request = new ReservationRequest(
                    LocalDate.now().plusDays(1),
                    1L, 1L);
            final LoginMember loginMember = new LoginMember("로키", "asd@email.com", MemberRole.MEMBER);
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationNotExistsMemberException.class);
        }

        @DisplayName("이미 해당 시간, 날짜에 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            // dummy 시간이 12시 40분
            final ReservationRequest request = new ReservationRequest(
                    LocalDate.now().plusDays(1),
                    1L, 1L);
            final LoginMember loginMember = new LoginMember("로키", "asd@email.com", MemberRole.MEMBER);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));
            fakeReservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(1)),
                    1L, 1L, 1L
            );

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationConflictException.class);
        }

        @DisplayName("과거 날짜로 예약하려면 예외가 발생한다.")
        @Test
        void create4() {
            // given
            final ReservationRequest request = new ReservationRequest(
                    LocalDate.now().minusDays(1),
                    1L, 1L);
            final LoginMember loginMember = new LoginMember("로키", "asd@email.com", MemberRole.MEMBER);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.create(request, loginMember);
            }).isInstanceOf(ReservationPastDateException.class);
        }

        @DisplayName("오늘의 지나간 시간으로 예약하려고하면 예외가 발생한다.")
        @Test
        void create5() {
            // given
            final ReservationRequest request = new ReservationRequest(
                    LocalDate.now(),
                    1L, 1L);
            final LoginMember loginMember = new LoginMember("로키", "asd@email.com", MemberRole.MEMBER);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.now().minusHours(1)));
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

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
            final AdminReservationRequest request = new AdminReservationRequest(
                    LocalDate.now().plusDays(1), 1L, 1L, 1L);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

            // when
            final ReservationResponse response = reservationService.createForAdmin(request);

            // then
            assertSoftly(s -> {
                s.assertThat(response.id()).isNotNull();
                s.assertThat(response.date()).isEqualTo(request.date());
            });
        }

        @DisplayName("테마가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create1() {
            // given
            final AdminReservationRequest request = new AdminReservationRequest(
                    LocalDate.now().plusDays(1), 1L, 1L, 1L);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationNotExistsThemeException.class);
        }

        @DisplayName("시간이 존재하지 않으면 예외가 발생한다.")
        @Test
        void create2() {
            // given
            final AdminReservationRequest request = new AdminReservationRequest(
                    LocalDate.now().plusDays(1), 1L, 1L, 1L);
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationNotExistsTimeException.class);
        }

        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        @Test
        void create6() {
            // given
            final AdminReservationRequest request = new AdminReservationRequest(
                    LocalDate.now().plusDays(1), 1L, 1L, 1L);
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationNotExistsMemberException.class);
        }

        @DisplayName("이미 해당 시간, 날짜, 테마에 예약이 존재한다면 예외가 발생한다.")
        @Test
        void create3() {
            // given
            // dummy 시간이 12시 40분
            final AdminReservationRequest request = new AdminReservationRequest(
                    LocalDate.now().plusDays(1), 1L, 1L, 1L);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));
            fakeReservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(1)),
                    1L, 1L, 2L
            );


            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationConflictException.class);
        }

        @DisplayName("과거 날짜로 예약하려면 예외가 발생한다.")
        @Test
        void create4() {
            // given
            final AdminReservationRequest request = new AdminReservationRequest(
                    LocalDate.now().minusDays(1), 1L, 1L, 1L);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 40)));
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

            // when & then
            assertThatThrownBy(() -> {
                reservationService.createForAdmin(request);
            }).isInstanceOf(ReservationPastDateException.class);
        }

        @DisplayName("오늘의 지나간 시간으로 예약하려고하면 예외가 발생한다.")
        @Test
        void create5() {
            // given
            final AdminReservationRequest request = new AdminReservationRequest(
                    LocalDate.now(), 1L, 1L, 1L);
            fakeReservationTimeRepository.save(new ReservationTime(LocalTime.now().minusHours(1)));
            fakeThemeRepository.save(new Theme("1", "2", "3"));
            fakeMemberRepository.saveMember(new Member("asd@email.com", "password", "boogie", MemberRole.MEMBER));

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
            // given & when
            final List<ReservationResponse> allReservation = reservationService.readAll();

            // then
            assertThat(allReservation).hasSize(0);
        }

        @DisplayName("존재하는 reservation들을 모두 조회한다.")
        @Test
        void readAll2() {
            // given
            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2026, 12, 1)),
                    1L, 1L, 1L
            );

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

            // when
            final List<ReservationResponse> responses =
                    reservationService.readAllByMemberAndThemeAndDateRange(request);

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

            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2024, 6, 15)),
                    1L, request.themeId(), request.memberId()
            );
            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2024, 7, 20)),
                    1L, request.themeId(), request.memberId()
            );

            // when
            final List<ReservationResponse> responses =
                    reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).hasSize(2);
        }

        @DisplayName("날짜 범위를 벗어난 예약은 조회되지 않는다")
        @Test
        void readAllByMemberAndThemeAndDateRange3() {
            // given
            final AdminFilterReservationRequest request = new AdminFilterReservationRequest(
                    1L, 1L,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 6, 30)
            );

            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2024, 6, 15)),
                    1L, request.themeId(), request.memberId()
            );
            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2024, 7, 20)),
                    1L, request.themeId(), request.memberId()
            );

            // when
            final List<ReservationResponse> responses =
                    reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).hasSize(1);
        }

        @DisplayName("다른 멤버의 예약은 조회되지 않는다")
        @Test
        void readAllByMemberAndThemeAndDateRange4() {
            // given
            final AdminFilterReservationRequest request = new AdminFilterReservationRequest(
                    1L, 1L,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );
            final Long otherMemberId = 2L;

            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2024, 6, 15)),
                    1L, request.themeId(), request.memberId()
            );
            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2024, 7, 20)),
                    1L, request.themeId(), otherMemberId
            );

            // when
            final List<ReservationResponse> responses =
                    reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).hasSize(1);
        }

        @DisplayName("다른 테마의 예약은 조회되지 않는다")
        @Test
        void readAllByMemberAndThemeAndDateRange5() {
            // given
            final AdminFilterReservationRequest request = new AdminFilterReservationRequest(
                    1L, 1L,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );
            final Long otherThemeId = 2L;

            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2024, 6, 15)),
                    1L, request.themeId(), request.memberId()
            );
            fakeReservationRepository.save(
                    new Reservation(LocalDate.of(2024, 7, 20)),
                    1L, otherThemeId, request.memberId()
            );

            // when
            final List<ReservationResponse> responses =
                    reservationService.readAllByMemberAndThemeAndDateRange(request);

            // then
            assertThat(responses).hasSize(1);
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
            fakeReservationRepository.save(new Reservation(LocalDate.of(2026, 12, 1)), 1L, 1L, 1L);

            // when
            reservationService.deleteById(id);

            // then
            assertThat(fakeReservationRepository.isInvokeDeleteById(id)).isTrue();
        }

        @DisplayName("주어진 id에 해당하는 reservation이 없다면 예외가 발생한다.")
        @Test
        void delete2() {
            // given & when & then
            assertThatThrownBy(() -> {
                reservationService.deleteById(1L);
            }).isInstanceOf(ReservationNotFoundException.class);
        }

    }


}
