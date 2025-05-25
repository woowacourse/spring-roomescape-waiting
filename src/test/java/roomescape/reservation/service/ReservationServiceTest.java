package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.member.repository.FakeMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberConverter;
import roomescape.member.service.usecase.MemberQueryUseCase;
import roomescape.reservation.controller.dto.CreateReservationWebRequest;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;
import roomescape.reservation.repository.FakeReservationRepository;
import roomescape.reservation.repository.FakeWaitingRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.service.usecase.ReservationCommandUseCase;
import roomescape.reservation.service.usecase.ReservationQueryUseCase;
import roomescape.reservation.service.usecase.WaitingCommandUseCase;
import roomescape.reservation.service.usecase.WaitingQueryUseCase;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ReservationServiceTest {

    private ReservationService reservationService;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository();
        final ReservationTimeQueryUseCase reservationTimeQueryUseCase = new ReservationTimeQueryUseCase(
                reservationTimeRepository);

        final ReservationQueryUseCase reservationQueryUseCase = new ReservationQueryUseCase(
                reservationRepository,
                reservationTimeQueryUseCase
        );

        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();
        final ReservationCommandUseCase reservationCommandUseCase = new ReservationCommandUseCase(
                reservationRepository,
                reservationQueryUseCase,
                reservationTimeQueryUseCase,
                new ThemeQueryUseCase(themeRepository),
                new MemberQueryUseCase(memberRepository)
        );

        waitingRepository = new FakeWaitingRepository();
        final WaitingCommandUseCase waitingCommandUseCase = new WaitingCommandUseCase(
                waitingRepository,
                new WaitingQueryUseCase(waitingRepository),
                reservationTimeQueryUseCase,
                new ThemeQueryUseCase(themeRepository),
                new MemberQueryUseCase(memberRepository)
        );
        final WaitingQueryUseCase waitingQueryUseCase = new WaitingQueryUseCase(waitingRepository);

        reservationService = new ReservationService(
                reservationQueryUseCase,
                reservationCommandUseCase,
                waitingCommandUseCase,
                waitingQueryUseCase
        );
    }

    @Test
    void 예약이_존재한다면_대기를_생성한다() {
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
                        MemberName.from("siso"),
                        MemberEmail.from("123123@gmail.com"),
                        Role.MEMBER
                )
        );

        final CreateReservationWebRequest request = new CreateReservationWebRequest(
                LocalDate.now().plusYears(1),
                reservationTime.getId(),
                theme.getId()
        );
        final CreateReservationWebRequest request2 = new CreateReservationWebRequest(
                request.date(),
                request.timeId(),
                request.themeId()
        );
        reservationService.create(request, MemberConverter.toDto(member));

        // When
        final ReservationWithStatusResponse response = reservationService.create(request2,
                MemberConverter.toDto(member2));

        // Then
        assertThat(waitingRepository.findById(response.id()))
                .isPresent();
    }

    @Test
    void 본인의_예약이_존재한다면_대기를_생성하지_않고_예외가_발생한다() {
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

        final CreateReservationWebRequest request = new CreateReservationWebRequest(
                LocalDate.now().plusYears(1),
                reservationTime.getId(),
                theme.getId()
        );
        final CreateReservationWebRequest request2 = new CreateReservationWebRequest(
                request.date(),
                request.timeId(),
                request.themeId()
        );
        reservationService.create(request, MemberConverter.toDto(member));

        // When & Then
        assertThatThrownBy(() -> reservationService.create(request2, MemberConverter.toDto(member)))
                .isInstanceOf(AlreadyExistException.class);
    }

    @Test
    void 예약_대기가_존재할_때_예약을_삭제하면_가장_빠른_대기가_예약_상태가_된다() {
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
                        MemberName.from("시송"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.MEMBER
                )
        );

        final CreateReservationWebRequest request = new CreateReservationWebRequest(
                LocalDate.now().plusYears(1),
                reservationTime.getId(),
                theme.getId()
        );
        final CreateReservationWebRequest request2 = new CreateReservationWebRequest(
                request.date(),
                request.timeId(),
                request.themeId()
        );

        reservationService.create(request, MemberConverter.toDto(member));
        reservationService.create(request2, MemberConverter.toDto(member2));

        // When
        reservationService.delete(1L);

        // Then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(waitingRepository.findAll())
                    .hasSize(0);
            softly.assertThat(reservationRepository.findAll())
                    .hasSize(1);
        });
    }

    @Test
    void 예약_대기가_존재하지_않을_때_예약을_삭제하면_예약만_삭제된다() {
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

        final CreateReservationWebRequest request = new CreateReservationWebRequest(
                LocalDate.now().plusYears(1),
                reservationTime.getId(),
                theme.getId()
        );

        reservationService.create(request, MemberConverter.toDto(member));

        // When
        reservationService.delete(1L);

        // Then
        assertThat(reservationRepository.findAll())
                .isEmpty();
    }
}
