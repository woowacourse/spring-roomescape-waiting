package roomescape.reservation.service.usecase;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.member.repository.FakeMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.usecase.MemberQueryUseCase;
import roomescape.reservation.repository.FakeWaitingRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.service.dto.CreateReservationWithMemberIdServiceRequest;
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

class WaitingCommandUseCaseTest {

    private WaitingRepository waitingRepository;
    private WaitingQueryUseCase waitingQueryUseCase;
    private WaitingCommandUseCase waitingCommandUseCase;
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

        waitingCommandUseCase = new WaitingCommandUseCase(
                waitingRepository,
                waitingQueryUseCase,
                new ReservationTimeQueryUseCase(reservationTimeRepository),
                new ThemeQueryUseCase(themeRepository),
                new MemberQueryUseCase(memberRepository)
        );
    }

    @Test
    void 예약_대기를_생성할_수_있다() {
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

        final CreateReservationWithMemberIdServiceRequest request = new CreateReservationWithMemberIdServiceRequest(
                1L,
                LocalDate.of(2025, 8, 10),
                1L,
                1L
        );

        // When & Then
        assertThat(waitingCommandUseCase.create(request))
                .isNotNull();
    }

    @Test
    void 날짜_테마_시간이_동일한_본인의_예약_대기가_존재할_때_추가로_대기를_시도하면_예외가_발생한다() {
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

        final CreateReservationWithMemberIdServiceRequest request = new CreateReservationWithMemberIdServiceRequest(
                1L,
                LocalDate.of(2025, 8, 10),
                1L,
                1L
        );

        waitingCommandUseCase.create(request);

        // When & Then
        assertThatThrownBy(() -> waitingCommandUseCase.create(request))
                .isInstanceOf(AlreadyExistException.class);
    }
}
