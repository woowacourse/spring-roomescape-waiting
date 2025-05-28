package roomescape.reservation.waiting.application;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseServiceTest;
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.domain.ReservationTimeRepository;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.waiting.domain.WaitingReservationRepository;
import roomescape.reservation.waiting.presentation.dto.WaitingReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

class WaitingReservationApplicationServiceTest extends BaseServiceTest {

    @Autowired
    private WaitingReservationApplicationService waitingReservationApplicationService;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약이 있는 경우 예약 대기를 할 수 있다.")
    @Test
    void can_wait_for_reserve() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member));
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.now().plusDays(1),
            reservationTime.getId(), theme.getId());

        // when
        WaitingReservationResponse waitingReservation = waitingReservationApplicationService.createWaitingReservation(
            reservationRequest, member.getId());

        // then
        Assertions.assertThat(waitingReservationRepository.findById(waitingReservation.id()))
            .isNotNull();
    }

    @DisplayName("중복 예약 대기를 할 수 없다.")
    @Test
    void not_accept_duplicate_wait() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member));
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.now().plusDays(1),
            reservationTime.getId(), theme.getId());

        // when
        waitingReservationApplicationService.createWaitingReservation(reservationRequest, member.getId());

        // then
        Assertions.assertThatThrownBy(() -> waitingReservationApplicationService.createWaitingReservation(reservationRequest, member.getId()))
            .isInstanceOf(BusinessException.class);
    }

    @DisplayName("없는 예약을 대기할 수 없다.")
    @Test
    void not_accept_waiting_if_reservation_not_exist() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.now().plusDays(1),
            reservationTime.getId(), theme.getId());

        // when-then
        Assertions.assertThatThrownBy(() -> waitingReservationApplicationService.createWaitingReservation(reservationRequest, member.getId()))
            .isInstanceOf(BusinessException.class);
    }

    @DisplayName("대기 예약을 승인할 수 있다.")
    @Test
    void can_accept_waiting_reservation() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        WaitingReservation waitingReservation = waitingReservationRepository.save(
            new WaitingReservation(LocalDate.now().plusDays(1), reservationTime, theme, member));

        // when
        waitingReservationApplicationService.acceptWaiting(waitingReservation.getId());

        // then
        Assertions.assertThat(waitingReservationRepository.existsById(waitingReservation.getId()))
            .isFalse();
        Assertions.assertThat(reservationRepository.findAll())
            .isNotEmpty();
    }

    @DisplayName("대기 예약을 거절할 수 있다.")
    @Test
    void can_deny_waiting_reservation() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        Member member = memberRepository.save(new Member(new Name("t"), new Email("t@e.com"), new Password("123")));
        WaitingReservation waitingReservation = waitingReservationRepository.save(
            new WaitingReservation(LocalDate.now().plusDays(1), reservationTime, theme, member));

        // when
        waitingReservationApplicationService.denyWaiting(waitingReservation.getId());

        // then
        Assertions.assertThat(waitingReservationRepository.existsById(waitingReservation.getId()))
            .isFalse();
        Assertions.assertThat(reservationRepository.findAll())
            .isEmpty();
    }
}
