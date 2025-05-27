package roomescape.reservation.service.usecase;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.member.repository.FakeMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.FakeWaitingRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.theme.repository.FakeThemeRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.FakeReservationTimeRepository;
import roomescape.time.repository.ReservationTimeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitingQueryUseCaseTest {

    private WaitingRepository waitingRepository;
    private WaitingQueryUseCase waitingQueryUseCase;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        waitingRepository = new FakeWaitingRepository();
        waitingQueryUseCase = new WaitingQueryUseCase(waitingRepository);

        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
    }

    @Test
    void 예약_대기_정보를_조회할_수_있다() {
        // Given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(18, 0)
                )
        );

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")
                )
        );

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final Waiting waiting = waitingRepository.save(
                Waiting.withoutId(
                        member,
                        ReservationDate.from(LocalDate.of(2025, 8, 10)),
                        reservationTime,
                        theme
                )
        );

        // When & Then
        assertThat(waitingQueryUseCase.get(waiting.getId()))
                .isEqualTo(waiting);
    }

    @Test
    void 예약_대기_정보가_없다면_조회_시_예외가_발생한다() {
        // Given
        final Long thrownId = 1L;

        // When & Then
        assertThatThrownBy(() -> waitingQueryUseCase.get(thrownId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void 전체_예약_대기_정보를_조회할_수_있다() {
        // Given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(18, 0)
                )
        );

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")
                )
        );

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final Waiting waiting = waitingRepository.save(
                Waiting.withoutId(
                        member,
                        ReservationDate.from(LocalDate.of(2025, 8, 10)),
                        reservationTime,
                        theme
                )
        );

        // When & Then
        assertThat(waitingQueryUseCase.getAll())
                .hasSize(1);
    }

    @Test
    void 날짜_timeId_themeId_memberId에_해당하는_대기_존재여부를_확인할_수_있다() {
        // Given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(18, 0)
                )
        );

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")
                )
        );

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final ReservationDate reservationDate = ReservationDate.from(LocalDate.of(2025, 8, 10));

        final Waiting waiting = waitingRepository.save(
                Waiting.withoutId(
                        member,
                        reservationDate,
                        reservationTime,
                        theme
                )
        );

        final Long timeId = reservationTime.getId();
        final Long themeId = theme.getId();
        final Long memberId = member.getId();

        // When & Then
        assertThat(waitingQueryUseCase.existsByParams(reservationDate, timeId, themeId, memberId))
                .isTrue();
    }

    @Test
    void 날짜_timeId_themeId에_해당하는_대기_존재여부를_확인할_수_있다() {
        // Given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(18, 0)
                )
        );

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")
                )
        );

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );

        final ReservationDate reservationDate = ReservationDate.from(LocalDate.of(2025, 8, 10));

        final Waiting waiting = waitingRepository.save(
                Waiting.withoutId(
                        member,
                        reservationDate,
                        reservationTime,
                        theme
                )
        );

        final Long timeId = reservationTime.getId();
        final Long themeId = theme.getId();

        // When & Then
        assertThat(waitingQueryUseCase.existsByParams(reservationDate, timeId, themeId))
                .isTrue();
    }

    @Test
    void 대기_순위를_확인할_수_있다() {
        // Given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(18, 0)
                )
        );

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")
                )
        );

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );
        final Member member2 = memberRepository.save(
                Member.withoutId(
                        MemberName.from("소시"),
                        MemberEmail.from("12345@gmail.com"),
                        Role.MEMBER
                )
        );

        final ReservationDate reservationDate = ReservationDate.from(LocalDate.of(2025, 8, 10));

        final Waiting waiting = waitingRepository.save(
                Waiting.withoutId(
                        member,
                        reservationDate,
                        reservationTime,
                        theme
                )
        );
        final Waiting waiting2 = waitingRepository.save(
                Waiting.withoutId(
                        member2,
                        reservationDate,
                        reservationTime,
                        theme
                )
        );

        // When & Then
        assertThat(waitingQueryUseCase.getWaitingWithRank(member2.getId()).getFirst().rank())
                .isEqualTo(2);
    }

    @Test
    void 대기_중_가장_빠른_대기_정보를_조회할_수_있다() {
        // Given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(18, 0)
                )
        );

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")
                )
        );

        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("강산"),
                        MemberEmail.from("123@gmail.com"),
                        Role.MEMBER
                )
        );
        final Member member2 = memberRepository.save(
                Member.withoutId(
                        MemberName.from("소시"),
                        MemberEmail.from("12345@gmail.com"),
                        Role.MEMBER
                )
        );

        final ReservationDate reservationDate = ReservationDate.from(LocalDate.of(2025, 8, 10));

        final Waiting waiting = waitingRepository.save(
                Waiting.withoutId(
                        member,
                        reservationDate,
                        reservationTime,
                        theme
                )
        );
        final Waiting waiting2 = waitingRepository.save(
                Waiting.withoutId(
                        member2,
                        reservationDate,
                        reservationTime,
                        theme
                )
        );

        // When & Then
        assertThat(waitingQueryUseCase.getEarliest(reservationDate, reservationTime.getId(), theme.getId()))
                .isEqualTo(waiting);
    }
}
