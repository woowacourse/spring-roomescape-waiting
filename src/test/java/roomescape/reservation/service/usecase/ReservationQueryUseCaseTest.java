package roomescape.reservation.service.usecase;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.member.repository.FakeMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.repository.FakeReservationRepository;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.service.dto.AvailableReservationTimeServiceResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.repository.FakeThemeRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.FakeReservationTimeRepository;
import roomescape.time.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.time.service.usecase.ReservationTimeQueryUseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationQueryUseCaseTest {

    private ReservationQueryUseCase reservationQueryUseCase;
    private ReservationRepository reservationRepository;

    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;

    private ReservationTime reservationTime;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();
        reservationQueryUseCase = new ReservationQueryUseCase(reservationRepository, new ReservationTimeQueryUseCase(reservationTimeRepository));

        reservationTime = reservationTimeRepository.save(ReservationTime.withoutId(LocalTime.of(18, 0)));
    }

    @Test
    @DisplayName("예약을 조회할 수 있다")
    void createAndFindReservation() {
        // given
        final Theme theme = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Account.of(Member.withoutId(
                                MemberName.from("강산"),
                                MemberEmail.from("123@gmail.com"),
                                Role.MEMBER),
                        Password.from("1234")));

        final Reservation given1 = Reservation.withoutId(
                member,
                ReservationDate.from(LocalDate.now().plusDays(1)),
                reservationTime,
                theme);

        final Reservation given2 = Reservation.withoutId(
                member,
                ReservationDate.from(LocalDate.now().plusDays(1)),
                reservationTime,
                theme);

        final Reservation saved1 = reservationRepository.save(given1);
        final Reservation saved2 = reservationRepository.save(given2);

        // when
        final List<Reservation> reservations = reservationQueryUseCase.getAll();

        // then
        assertThat(reservations).hasSize(2);
        final Reservation found1 = reservations.getFirst();
        final Reservation found2 = reservations.get(1);

        assertAll(() -> {
            assertThat(found1).isEqualTo(saved1);
            assertThat(found2).isEqualTo(saved2);
        });
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대한 예약 가능 여부가 포함된 시간 정보를 받을 수 있다")
    void getTimesWithAvailability() {
        // given
        final ReservationTime booked = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(10, 18)));

        final ReservationTime unbooked = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(22, 45)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Account.of(Member.withoutId(
                                MemberName.from("강산"),
                                MemberEmail.from("123@gmail.com"),
                                Role.MEMBER),
                        Password.from("1234"))
        );

        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));

        final Reservation reservation = reservationRepository.save(Reservation.withoutId(
                member,
                date,
                booked,
                theme));

        // when
        final List<AvailableReservationTimeServiceResponse> timesWithAvailability = reservationQueryUseCase.getTimesWithAvailability(
                new AvailableReservationTimeServiceRequest(date.getValue(), theme.getId()));

        // then
        SoftAssertions.assertSoftly(softAssertions -> {

            assertThat(timesWithAvailability)
                    .hasSize(3);

            assertThat(timesWithAvailability.stream().filter(AvailableReservationTimeServiceResponse::isBooked))
                    .hasSize(1);

            assertThat(timesWithAvailability.stream()
                    .filter(AvailableReservationTimeServiceResponse::isBooked)
                    .map(AvailableReservationTimeServiceResponse::startAt)
                    .findFirst()
                    .orElseThrow()
            ).isEqualTo(booked.getValue());

        });
    }

    @Test
    void 멤버ID를_통해_예약_정보를_조회한다() {
        final ReservationTime booked = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(10, 18)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Account.of(Member.withoutId(
                                MemberName.from("강산"),
                                MemberEmail.from("123@gmail.com"),
                                Role.MEMBER),
                        Password.from("1234"))
        );

        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));

        final Reservation reservation = reservationRepository.save(Reservation.withoutId(
                member,
                date,
                booked,
                theme));

        // when & then
        assertThat(reservationQueryUseCase.getAllByMemberId(member.getId()))
                .contains(reservation);
    }

    @Test
    void 멤버ID_시작일_종료일_테마ID로_예약된_정보를_조회한다() {
        final ReservationTime booked = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(10, 18)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Account.of(Member.withoutId(
                                MemberName.from("강산"),
                                MemberEmail.from("123@gmail.com"),
                                Role.MEMBER),
                        Password.from("1234"))
        );

        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        final ReservationDate from = ReservationDate.from(LocalDate.now());
        final ReservationDate to = ReservationDate.from(LocalDate.now().plusDays(2));

        final Reservation reservation = reservationRepository.save(Reservation.withoutId(
                member,
                date,
                booked,
                theme));

        // when & then
        assertThat(reservationQueryUseCase.search(member.getId(), theme.getId(), from, to))
                .contains(reservation);
    }
}
