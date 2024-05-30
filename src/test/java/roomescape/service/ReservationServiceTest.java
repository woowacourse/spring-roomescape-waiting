package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.NotFoundException;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@Import(ReservationService.class)
class ReservationServiceTest extends ServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private MemberReservationRepository memberReservationRepository;


    @Test
    @DisplayName("동일한 날짜와 시간과 테마에 예약을 생성하면 예외가 발생한다")
    void duplicateTimeReservationAddFail() {
        // given
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        Member member = memberFixture.createMember();
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        // when & then
        reservationFixture.createReservation(reservationDetail, member);

        assertThatThrownBy(() -> reservationService.addMemberReservation(
                new ReservationRequest(tomorrow, time.getId(), theme.getId()), member.getId(), ReservationStatus.RESERVED))
                .isInstanceOf(DataDuplicateException.class);
    }

    @Test
    @DisplayName("이미 지난 날짜로 예약을 생성하면 예외가 발생한다")
    void beforeDateReservationFail() {
        // given
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        Member member = memberFixture.createMember();
        LocalDate yesterday = LocalDate.now().minusDays(1L);
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(yesterday, time, theme);
        reservationFixture.createReservation(reservationDetail, member);

        // when & then
        assertThatThrownBy(() -> reservationService.addMemberReservation(
                new ReservationRequest(yesterday, time.getId(), theme.getId()), member.getId(), ReservationStatus.RESERVED))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("예약하려는 날짜가 당일일 때, 이미 지난 시간으로 예약을 생성하면 예외가 발생한다")
    void beforeTimeReservationFail() {
        // given
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        Member member = memberFixture.createMember();
        LocalDate yesterday = LocalDate.now().minusDays(1L);

        // when & then
        assertThatThrownBy(() -> reservationService.addMemberReservation(
                new ReservationRequest(yesterday, time.getId(), theme.getId()), member.getId(), ReservationStatus.RESERVED))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("존재하지 않는 회원이 예약을 생성하려고 하면 예외를 발생한다.")
    void notExistMemberReservationFail() {
        // given
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        Long NotExistMemberId = 1L;

        // when & then
        assertThatThrownBy(() -> reservationService.addMemberReservation(
                new ReservationRequest(tomorrow, time.getId(), theme.getId()), NotExistMemberId, ReservationStatus.RESERVED))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약 상태에 따른 예약정보를 조회한다.")
    void findReservedReservations() {
        // given
        ReservationTime time = reservationTimeFixture.createTime();

        Theme theme = themeFixture.createTheme();
        Member member = memberFixture.createMember();
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);
        reservationFixture.createReservation(reservationDetail, member);
        reservationFixture.createWaiting(reservationDetail, member);

        // when
        List<MemberReservation> reservedReservations = memberReservationRepository.findByStatus(ReservationStatus.RESERVED);
        List<MemberReservation> waitingReservations = memberReservationRepository.findByStatus(ReservationStatus.WAITING);

        // then
        assertAll(
                () -> Assertions.assertThat(reservedReservations).hasSize(1),
                () -> Assertions.assertThat(waitingReservations).hasSize(1)
        );
    }

    @Test
    @DisplayName("첫 번째 순서로 대기 중인 예약 대기 정보들을 조회한다.")
    void findFirstOrderWaitingReservations() {
        // given
        Theme theme1 = themeFixture.createTheme();
        Theme theme2 = themeFixture.createTheme();
        ReservationTime time = reservationTimeFixture.createTime();
        Member member = memberFixture.createMember();
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationDetail theme1Detail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme1);
        ReservationDetail theme2Detail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme2);

        reservationFixture.createWaiting(theme1Detail, member);
        reservationFixture.createWaiting(theme2Detail, member);

        // when & then
        List<ReservationResponse> firstOrderWaitingReservations = reservationService.findFirstOrderWaitingReservations().reservations();
        Assertions.assertThat(firstOrderWaitingReservations).hasSize(2);
    }

    @Test
    @DisplayName("예약 승인을 요청한 대기 중인 예약을 승인 시, 같은 날짜/시간/테마에 이미 예약이 있어도 가능한 것으로 판단하고 하나 더 받는다.")
    void acceptReservationWaiting() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        Theme theme = themeFixture.createTheme();
        ReservationTime time = reservationTimeFixture.createTime();
        Member reserveMember = memberFixture.createMember();
        Member waitingMember = memberFixture.createMember();
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        reservationFixture.createReservation(reservationDetail, reserveMember);
        reservationFixture.createWaiting(reservationDetail, waitingMember);

        // when
        reservationService.approveWaitingReservation(waitingMember.getId());

        // then
        List<ReservationResponse> reservedReservations = reservationService.findReservationsByStatus(ReservationStatus.RESERVED).reservations();
        Assertions.assertThat(reservedReservations).hasSize(2);
    }

    @Test
    @DisplayName("예약 승인을 요청한 대기 중인 예약이, 1순위 대기 상태가 아니라면 예약 대기 상태를 예약 상태로 변경할 수 없다.")
    void cannotAcceptReservationWaitingBecauseReservationOrderIsNotFirst() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();

        Member reserveMember = memberFixture.createMember();
        Member firstWaitingMember = memberFixture.createMember();
        Member secondWaitingMember = memberFixture.createMember();
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        reservationFixture.createReservation(reservationDetail, reserveMember);
        reservationFixture.createReservation(reservationDetail, firstWaitingMember);
        MemberReservation secondWaitingReservation = reservationFixture.createReservation(reservationDetail, secondWaitingMember);

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.approveWaitingReservation(secondWaitingReservation.getId()))
                .isInstanceOf(ValidateException.class);

    }

    @Test
    @DisplayName("이미 예약이 존재하는 날짜/시간/테마에 예약 생성을 수행하면 예외가 발생한다.")
    void validateDateTimeThemeDuplication() {
        // given
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        Member member = memberFixture.createMember();
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);
        reservationFixture.createReservation(reservationDetail, member);

        // when & then
        Member anotherMember = memberFixture.createMember();
        Assertions.assertThatThrownBy(() -> reservationService.addMemberReservation(
                        new ReservationRequest(tomorrow, time.getId(), theme.getId()), anotherMember.getId(), ReservationStatus.RESERVED))
                .isInstanceOf(DataDuplicateException.class);
    }
}
