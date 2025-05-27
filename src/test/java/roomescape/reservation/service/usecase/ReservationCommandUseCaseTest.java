package roomescape.reservation.service.usecase;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.AlreadyExistException;
import roomescape.common.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.member.repository.FakeMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.usecase.MemberQueryUseCase;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.FakeReservationRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.CreateReservationServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.theme.repository.FakeThemeRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.usecase.ThemeQueryUseCase;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.FakeReservationTimeRepository;
import roomescape.time.repository.ReservationTimeRepository;
import roomescape.time.service.usecase.ReservationTimeQueryUseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationCommandUseCaseTest {

    private ReservationCommandUseCase reservationCommandUseCase;
    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();

        reservationCommandUseCase = new ReservationCommandUseCase(
                reservationRepository,
                new ReservationQueryUseCase(reservationRepository,
                        new ReservationTimeQueryUseCase(reservationTimeRepository)),
                new ReservationTimeQueryUseCase(reservationTimeRepository),
                new ThemeQueryUseCase(themeRepository),
                new MemberQueryUseCase(memberRepository)
        );
    }

    @Test
    @DisplayName("예약을 생성할 수 있다")
    void createAndFindReservation() {
        // given

        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 27)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final CreateReservationServiceRequest requestDto = new CreateReservationServiceRequest(
                member.getId(),
                LocalDate.of(2025, 8, 5),
                reservationTime.getId(),
                theme.getId());

        // when
        final Reservation reservation = reservationCommandUseCase.create(requestDto);

        // then
        final Reservation found = reservationRepository.findById(reservation.getId())
                .orElseThrow(NoSuchElementException::new);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(found.getId()).isEqualTo(reservation.getId());
            softAssertions.assertThat(found.getMember()).isEqualTo(reservation.getMember());
            softAssertions.assertThat(found.getDate()).isEqualTo(reservation.getDate());
            softAssertions.assertThat(found.getTime()).isEqualTo(reservation.getTime());
            softAssertions.assertThat(found.getTheme()).isEqualTo(reservation.getTheme());
        });
    }

    @Test
    @DisplayName("중복된 예약을 생성할 수 없다.")
    void existsReservation() {
        // given
        final Theme theme = themeRepository.save(Theme.withoutId(
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com")));

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(LocalTime.of(12, 27)));

        final Reservation savedReservation = reservationCommandUseCase.create(
                new CreateReservationServiceRequest(
                        member.getId(),
                        LocalDate.of(2025, 8, 10),
                        reservationTime.getId(),
                        theme.getId()
                ));

        // When & Then
        assertThatThrownBy(() -> reservationCommandUseCase.create(
                new CreateReservationServiceRequest(
                        member.getId(),
                        LocalDate.of(2025, 8, 10),
                        reservationTime.getId(),
                        theme.getId())))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("추가하려는 예약이 이미 존재합니다.");
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다")
    void deleteReservation() {
        // given
        final Theme theme = Theme.withId(
                1L,
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com"));

        final Member member = Member.withId(
                1L,
                MemberName.from("강산"),
                MemberEmail.from("123@gmail.com"),
                Role.MEMBER);

        final ReservationTime reservationTime = ReservationTime.withId(
                1L,
                LocalTime.of(12, 27));

        final Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        member,
                        ReservationDate.from(LocalDate.of(2025, 8, 10)),
                        reservationTime,
                        theme));

        // when
        reservationCommandUseCase.delete(reservation.getId());

        // then
        assertThat(reservationRepository.findById(reservation.getId()).isPresent())
                .isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하려 하면 예외가 발생한다")
    void deleteNonExistentReservation() {
        // Given
        final Long id = 1000L;

        // When & Then
        assertThatThrownBy(() -> reservationCommandUseCase.delete(id))
                .isInstanceOf(NotFoundException.class);
    }
}
