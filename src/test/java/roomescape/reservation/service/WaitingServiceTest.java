package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialMemberFixture.ADMIN;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialMemberFixture.MEMBER_2;
import static roomescape.InitialMemberFixture.NO_RESERVATION_MEMBER;
import static roomescape.InitialReservationFixture.NO_RESERVATION_DATE;
import static roomescape.InitialReservationFixture.RESERVATION_1;
import static roomescape.InitialWaitingFixture.WAITING_1;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.NotFoundException;
import roomescape.member.dto.MemberRequest;
import roomescape.reservation.dto.ReservationOrWaitingResponse;
import roomescape.reservation.dto.WaitingRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.WaitingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("예약이 없는데 예약 대기를 신청하는 경우 예외가 발생한다.")
    void throwExceptionWhenAddWaitingWithoutReservation() {
        WaitingRequest waitingRequest = new WaitingRequest(
                NO_RESERVATION_DATE,
                RESERVATION_1.getReservationTime().getId(),
                RESERVATION_1.getTheme().getId()
        );
        MemberRequest memberRequest = new MemberRequest(MEMBER_1);
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequest, memberRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("이미 예약에 성공한 회원이 같은 예약에 대기를 걸 경우 예외를 발생시킨다.")
    void throwExceptionWhenSameMemberAddWaiting() {
        WaitingRequest waitingRequest = new WaitingRequest(
                RESERVATION_1.getDate(),
                RESERVATION_1.getReservationTime().getId(),
                RESERVATION_1.getTheme().getId()
        );
        MemberRequest memberRequest = new MemberRequest(MEMBER_1);
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequest, memberRequest))
                .isInstanceOf(DuplicationException.class);
    }

    @Test
    @DisplayName("이미 예약 대기를 걸어둔 회원이 같은 예약에 대기를 걸 경우 예외를 발생시킨다.")
    void throwExceptionWhenSameMemberAddWaitingAgain() {
        WaitingRequest waitingRequest = new WaitingRequest(
                RESERVATION_1.getDate(),
                RESERVATION_1.getReservationTime().getId(),
                RESERVATION_1.getTheme().getId()
        );
        MemberRequest memberRequest = new MemberRequest(MEMBER_2);
        assertThatThrownBy(() -> waitingService.addWaiting(waitingRequest, memberRequest))
                .isInstanceOf(DuplicationException.class);
    }

    @Test
    @DisplayName("예외 상황을 피해서 예약 대기를 신청하는 경우 잘 예약된다.")
    void addWaitingWithReservation() {
        WaitingRequest waitingRequest = new WaitingRequest(
                RESERVATION_1.getDate(),
                RESERVATION_1.getReservationTime().getId(),
                RESERVATION_1.getTheme().getId()
        );
        MemberRequest memberRequest = new MemberRequest(NO_RESERVATION_MEMBER);

        WaitingResponse waitingResponse = waitingService.addWaiting(waitingRequest, memberRequest);

        assertThat(waitingResponse.id()).isNotNull();
    }

    @Test
    @DisplayName("특정 회원이 가진 예약 대기 목록을 순위와 함께 가져온다.")
    void getWaitingsByMember() {
        MemberRequest memberRequest = new MemberRequest(MEMBER_2);

        List<ReservationOrWaitingResponse> waitingsByMember = waitingService.findWaitingsByMember(memberRequest);

        assertThat(waitingsByMember).containsExactly(
                new ReservationOrWaitingResponse(
                        WAITING_1.getId(),
                        WAITING_1.getTheme().getName().name(),
                        WAITING_1.getDate(DateTimeFormatter.ISO_DATE),
                        WAITING_1.getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                        "1번째 예약대기"
                )
        );
    }

    @Test
    @DisplayName("예약 대기 삭제 권한이 없는 회원이 예약 대기 삭제를 시도할 경우 예외를 발생시킨다.")
    void throwExceptionIfNotRelatedMemberDeleteWaiting() {
        MemberRequest memberRequest = new MemberRequest(MEMBER_1);

        assertThatThrownBy(() -> waitingService.deleteWaiting(WAITING_1.getId(), memberRequest))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("자신이 예약한 예약 대기는 삭제할 수 있다.")
    void MemberWhoWaitsCanDeleteWaiting() {
        MemberRequest memberRequest = new MemberRequest(WAITING_1.getMember());

        waitingService.deleteWaiting(WAITING_1.getId(), memberRequest);

        assertThat(waitingRepository.existsByDateAndReservationTimeAndThemeAndMember(
                WAITING_1.getDate(),
                WAITING_1.getReservationTime(),
                WAITING_1.getTheme(),
                WAITING_1.getMember()
        )).isFalse();
    }

    @Test
    @DisplayName("관리자는 모든 예약 대기를 삭제할 수 있다.")
    void AdminCanDeleteWaiting() {
        MemberRequest memberRequest = new MemberRequest(ADMIN);

        waitingService.deleteWaiting(WAITING_1.getId(), memberRequest);

        assertThat(waitingRepository.existsByDateAndReservationTimeAndThemeAndMember(
                WAITING_1.getDate(),
                WAITING_1.getReservationTime(),
                WAITING_1.getTheme(),
                WAITING_1.getMember()
        )).isFalse();
    }
}
