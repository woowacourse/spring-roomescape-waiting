package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.getMemberAdmin;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.MemberFixture.getMemberClover;
import static roomescape.fixture.MemberFixture.getMemberEden;
import static roomescape.fixture.ReservationFixture.getNextDayReservation;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.exception.AuthorizationException;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.dto.MyReservationProjection;
import roomescape.reservation.service.services.MemberReservationService;
import roomescape.reservation.service.services.WaitingReservationService;
import roomescape.util.ServiceTest;

@DisplayName("예약 대기 로직 테스트")
class WaitingReservationServiceTest extends ServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    WaitingReservationService waitingReservationService;

    @Autowired
    MemberReservationService memberReservationService;

    ReservationTime time;
    Theme theme1;
    Member memberChoco;

    @BeforeEach
    void setUp() {
        time = reservationTimeRepository.save(getNoon());
        theme1 = themeRepository.save(getTheme1());
        memberChoco = memberRepository.save(getMemberChoco());
    }

    @DisplayName("예약 대기에 성공한다.")
    @Test
    void addWaitingList() {
        //given
        Member memberClover = memberRepository.save(getMemberClover());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));

        //when
        MemberReservation waitingReservation = waitingReservationService.addWaiting(memberClover, reservation);
        List<MyReservationProjection> response = memberReservationRepository.findByMember(
                memberClover.getId());

        //then
        assertAll(
                () -> assertThat(waitingReservation.getReservationStatus()).isEqualTo(ReservationStatus.PENDING),
                () -> assertThat(response.get(0)).isNotNull(),
                () -> assertThat(response.get(0).getWaitingNumber()).isEqualTo(2)
        );
    }

    @DisplayName("모든 예약 대기 조회에 성공한다.")
    @Test
    void getWaiting() {
        //given
        Member memberClover = memberRepository.save(getMemberClover());
        Member memberEden = memberRepository.save(getMemberEden());

        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));
        memberReservationRepository.save(new MemberReservation(memberClover, reservation, ReservationStatus.PENDING));
        memberReservationRepository.save(new MemberReservation(memberEden, reservation, ReservationStatus.PENDING));

        //when
        List<ReservationResponse> waiting = waitingReservationService.getWaiting();

        //then
        assertThat(waiting).hasSize(2);
        assertThat(waiting).extracting("date").containsOnly(getNextDayReservation(time, theme1).getDate());
    }

    @DisplayName("대기 예약을 승인한다.")
    @Test
    void approve() {
        //given
        Member memberClover = memberRepository.save(getMemberClover());

        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));
        MemberReservation waitingReservation = memberReservationRepository.save(
                new MemberReservation(memberClover, reservation, ReservationStatus.PENDING));
        Member admin = memberRepository.findMemberByEmailAddress(getMemberAdmin().getEmail()).orElseThrow();

        //when
        waitingReservationService.approveWaiting(admin, waitingReservation);

        //then
        MemberReservation memberReservation = memberReservationRepository.findById(waitingReservation.getId())
                .orElseThrow(IllegalStateException::new);
        assertThat(memberReservation.getReservationStatus()).isEqualTo(ReservationStatus.APPROVED);
    }

    @DisplayName("대기 예약이 아닌 예약 승인 시, 예외가 발생한다.")
    @Test
    void approveNotWaitingReservation() {
        //given
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));
        Member admin = memberRepository.findMemberByEmailAddress(getMemberAdmin().getEmail()).orElseThrow();

        //when & then
        assertThatThrownBy(() -> waitingReservationService.approveWaiting(admin, memberReservation))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorType.NOT_A_WAITING_RESERVATION.getMessage());
    }

    @DisplayName("대기 예약을 거절한다.")
    @Test
    void deny() {
        //given
        Member memberClover = memberRepository.save(getMemberClover());

        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        memberReservationRepository.save(new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));
        MemberReservation waitingReservation = memberReservationRepository.save(
                new MemberReservation(memberClover, reservation, ReservationStatus.PENDING));
        Member admin = memberRepository.findMemberByEmailAddress(getMemberAdmin().getEmail()).orElseThrow();

        //when
        waitingReservationService.denyWaiting(admin, waitingReservation);

        //then
        MemberReservation memberReservation = memberReservationRepository.findById(waitingReservation.getId())
                .orElseThrow(IllegalStateException::new);
        assertThat(memberReservation.getReservationStatus()).isEqualTo(ReservationStatus.DENY);
    }

    @DisplayName("대기 예약이 아닌 예약 거절 시, 예외가 발생한다.")
    @Test
    void denyNotWaitingReservation() {
        //given
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));
        Member admin = memberRepository.findMemberByEmailAddress(getMemberAdmin().getEmail()).orElseThrow();

        //when & then
        assertThatThrownBy(() -> waitingReservationService.denyWaiting(admin, memberReservation))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorType.NOT_A_WAITING_RESERVATION.getMessage());
    }

    @DisplayName("관리자가 아닌 사용자가 승인 및 거절 시, 예외가 발생한다.")
    @Test
    void approveAndDenyPermissionException() {
        //given
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme1));
        MemberReservation memberReservation = memberReservationRepository.save(
                new MemberReservation(memberChoco, reservation, ReservationStatus.APPROVED));

        //when & then
        assertAll(
                () -> assertThatThrownBy(() -> waitingReservationService.approveWaiting(memberChoco, memberReservation))
                        .isInstanceOf(AuthorizationException.class)
                        .hasMessage(ErrorType.NOT_ALLOWED_PERMISSION_ERROR.getMessage()),
                () -> assertThatThrownBy(() -> waitingReservationService.denyWaiting(memberChoco, memberReservation))
                        .isInstanceOf(AuthorizationException.class)
                        .hasMessage(ErrorType.NOT_ALLOWED_PERMISSION_ERROR.getMessage())
        );
    }
}
