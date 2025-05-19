package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.command.dto.CreateReservationCommand;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.ReservationException;
import roomescape.infrastructure.error.exception.ReservationTimeException;
import roomescape.infrastructure.error.exception.ThemeException;

class CreateReservationServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private CreateReservationService createReservationService;

    @BeforeEach
    void setUp() {
        createReservationService = new CreateReservationService(
                reservationTimeRepository,
                reservationRepository,
                themeRepository,
                memberRepository,
                clock
        );
    }

    @Test
    void 예약을_생성할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));

        CreateReservationCommand command = new CreateReservationCommand(LocalDate.now(clock).plusDays(1), time.getId(),
                theme.getId(), member.getId());

        // when
        Long id = createReservationService.create(command);

        // then
        assertThat(reservationRepository.findById(id)).isPresent();
    }

    @Test
    void 존재하지_않는_회원으로_예약할_수_없다() {
        // given
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        CreateReservationCommand command = new CreateReservationCommand(
                LocalDate.now(clock),
                time.getId(),
                theme.getId(),
                999L
        );

        // when
        // then
        assertThatThrownBy(() -> createReservationService.create(command))
                .isInstanceOf(MemberException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    void 존재하지_않는_예약시간으로_예약할_수_없다() {
        // given
        Long invalidTimeId = 999L;
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        CreateReservationCommand command = new CreateReservationCommand(
                LocalDate.now(clock),
                invalidTimeId,
                theme.getId(),
                member.getId()
        );

        // when
        // then
        assertThatThrownBy(() -> createReservationService.create(command))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @Test
    void 존재하지_않는_테마로_예약할_수_없다() {
        // given
        Long invalidThemeId = 999L;
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        CreateReservationCommand command = new CreateReservationCommand(LocalDate.now(clock), time.getId(),
                invalidThemeId,
                member.getId());

        // when
        // then
        assertThatThrownBy(() -> createReservationService.create(command))
                .isInstanceOf(ThemeException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @Test
    void 같은_날짜와_같은_시간과_같은_테마에_예약이_존재한다면_예약을_생성하면_예외가_발생한다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock), time, theme));
        CreateReservationCommand command = new CreateReservationCommand(
                LocalDate.now(clock),
                time.getId(),
                theme.getId(),
                member.getId()
        );

        // when
        // then
        assertThatThrownBy(() -> createReservationService.create(command))
                .isInstanceOf(ReservationException.class)
                .hasMessage("날짜와 시간이 중복된 예약이 존재합니다.");
    }
}
