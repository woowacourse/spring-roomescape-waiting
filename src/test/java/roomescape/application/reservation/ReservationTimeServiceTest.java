package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.dto.AvailableReservationTimeResult;
import roomescape.application.reservation.dto.CreateReservationTimeParam;
import roomescape.application.reservation.dto.ReservationTimeResult;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.BusinessRuleViolationException;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

class ReservationTimeServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    void 운영시간_내의_예약시간을_생성할_수_있다() {
        // given
        CreateReservationTimeParam createReservationTimeParam = new CreateReservationTimeParam(LocalTime.of(12, 0));

        // when
        Long timeId = reservationTimeService.create(createReservationTimeParam);

        // then
        assertThat(reservationTimeRepository.findById(timeId))
                .isPresent()
                .hasValue(new ReservationTime(1L, LocalTime.of(12, 0)));
    }

    @Test
    void 운영시간_외의_예약시간을_생성할_수_없다() {
        // given
        CreateReservationTimeParam createReservationTimeParam = new CreateReservationTimeParam(LocalTime.of(4, 0));

        // when
        // then
        assertThatCode(() -> reservationTimeService.create(createReservationTimeParam))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("해당 시간은 예약 가능 시간이 아닙니다.");
    }

    @Test
    void 이미_존재하는_예약시간을_추가하는_경우_예외가_발생한다() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        CreateReservationTimeParam createReservationTimeParam = new CreateReservationTimeParam(LocalTime.of(12, 0));

        // when
        // then
        assertThatCode(() -> reservationTimeService.create(createReservationTimeParam))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("이미 존재하는 얘약시간입니다.");
    }

    @Test
    void 예약시간을_조회할_수_있다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));

        // when
        ReservationTimeResult result = reservationTimeService.findById(reservationTime.getId());

        // then
        assertThat(result).isEqualTo(new ReservationTimeResult(reservationTime.getId(), LocalTime.of(12, 0)));
    }

    @Test
    void 존재하지_않는_예약시간을_조회할_수_없다() {
        // given
        Long invalidId = 999L;

        // when
        // then
        assertThatCode(() -> reservationTimeService.findById(invalidId))
                .isInstanceOf(NotFoundEntityException.class)
                .hasMessage(invalidId + "에 해당하는 reservation_time 튜플이 없습니다.");
    }

    @Test
    void 모든_예약시간을_조회할_수_있다() {
        // given
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));

        // when
        List<ReservationTimeResult> results = reservationTimeService.findAll();

        // then
        assertThat(results).containsExactlyInAnyOrder(
                new ReservationTimeResult(1L, LocalTime.of(12, 0)),
                new ReservationTimeResult(2L, LocalTime.of(13, 0))
        );
    }

    @Test
    void 예약시간을_삭제할_수_있다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));

        // when
        reservationTimeService.deleteById(reservationTime.getId());

        // then
        assertThat(reservationTimeRepository.findById(1L))
                .isNotPresent();
    }

    @Test
    void 예약시간으로_예약된_예약이_존재하는_경우_예외가_발생한다() {
        // given
        Member member = memberRepository.save(new Member("test", new Email("test@test.com"), "test", Role.ADMIN));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), reservationTime, theme));

        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.deleteById(reservationTime.getId()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("해당 예약 시간에 예약이 존재합니다.");
    }

    @Test
    void 예약시간을_예약여부와_함께_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("test", new Email("test@test.com"), "test", Role.ADMIN));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), reservationTime1, theme));

        // when
        List<AvailableReservationTimeResult> availableTimesByThemeIdAndDate = reservationTimeService.findAvailableTimesByThemeIdAndDate(
                theme.getId(),
                LocalDate.now().plusDays(1)
        );

        // then
        assertThat(availableTimesByThemeIdAndDate).containsExactlyInAnyOrder(
                new AvailableReservationTimeResult(1L, LocalTime.of(12, 0), true),
                new AvailableReservationTimeResult(2L, LocalTime.of(13, 0), false)
        );
    }
}
