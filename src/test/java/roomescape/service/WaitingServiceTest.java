package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.TestDataInitExtension;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.domain.WaitingWithRank;
import roomescape.domain.dto.WaitingRequest;
import roomescape.exception.DeleteNotAllowException;
import roomescape.exception.ReservationFailException;

/*
 * 예약 대기 관련 초기 데이터
 * 예약 테이블
 * {ID=9, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=1, STATUS=RESERVED}
 * {ID=10, DATE=내일일자, TIME_ID=2, THEME_ID=1, MEMBER_ID=1, STATUS=RESERVED}
 * 예약 대기 테이블
 * {ID=1, DATE='2024-04-30', TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 * {ID=2, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 * {ID=3, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=3, STATUS=WAITING}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(TestDataInitExtension.class)
class WaitingServiceTest {
    private final WaitingService service;

    @Autowired
    public WaitingServiceTest(WaitingService waitingService) {
        this.service = waitingService;
    }

    @Test
    @DisplayName("특정 회원의 예약 대기 목록을 반환한다.")
    void given_member_when_findWaitingByMember_then_returnWaitings() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(2L, "admin@email.com", password, "admin", Role.ADMIN);
        //when
        List<WaitingWithRank> waitingsByMember = service.findWaitingsByMember(member);
        //then
        assertThat(waitingsByMember).hasSize(2);
    }

    @Test
    @DisplayName("해당 날짜, 시간, 테마의 예약이 있으면 예약대기할 수 있다.")
    void given_reservationExist_when_create_then_returnWaitingResponse() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        WaitingRequest request = new WaitingRequest(date, 2L, 1L, 2L);
        //when, then
        assertAll(
                () -> assertDoesNotThrow(() -> service.create(request)),
                () -> assertThat(service.findNotRejectedWaitingList()).hasSize(4)
        );
    }

    @Test
    @DisplayName("해당 날짜, 시간, 테마의 예약이 없으면 예약대기할 수 없다.")
    void given_reservationNonExist_when_create_then_throwException() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        WaitingRequest request = new WaitingRequest(date, 3L, 1L, 2L);
        //when, then
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ReservationFailException.class);
    }

    @Test
    @DisplayName("해당 날짜, 시간에 예약한 내용이 있으면 예약대기할 수 없다.")
    void given_sameDateAndTimeReservationExist_when_create_then_throwException() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        WaitingRequest request = new WaitingRequest(date, 1L, 1L, 1L);
        //when, then
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ReservationFailException.class);
    }

    @Test
    @DisplayName("해당 날짜, 시간에 대기중인 내역이 있으면 예약대기할 수 없다.")
    void given_waitingExist_when_create_then_throwException() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        WaitingRequest request = new WaitingRequest(date, 1L, 1L, 2L);
        //when, then
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ReservationFailException.class);
    }

    @Test
    @DisplayName("일반 사용자는 본인의 예약 대기를 삭제할 수 있다.")
    void given_existWaiting_when_deleteByUser_then_deleteWaiting() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(3L, "user2@email.com", password, "user2", Role.USER);
        //when, then
        assertDoesNotThrow(() -> service.delete(3L, member));
    }

    @Test
    @DisplayName("일반 사용자가 타인의 예약 대기 내역을 삭제할 수 없다.")
    void given_when_deleteOtherMembersWaiting_then_throwException() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(3L, "user2@email.com", password, "user2", Role.USER);
        //when, then
        assertThatThrownBy(() -> service.delete(2L, member)).isInstanceOf(DeleteNotAllowException.class);
    }

    @Test
    @DisplayName("일반 사용자가 존재하지 않는 예약 대기를 삭제하면 예외가 발생한다.")
    void given_NonExistWaiting_when_deleteByUser_then_throwException() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(3L, "user2@email.com", password, "user2", Role.USER);
        //when, then
        assertThatThrownBy(() -> service.delete(10L, member)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("관리자는 존재하는 예약 대기를 거절할 수 있다.")
    void given_existWaiting_when_rejectByAdmin_then_deleteWaiting() {
        //when, then
        assertDoesNotThrow(() -> service.rejectedByAdmin(3L));
    }

    @Test
    @DisplayName("관리자가 존재하지 않는 예약 대기를 거절하면 예외가 발생한다.")
    void given_NonExistWaiting_when_rejectByAdmin_then_throwException() {
        //when, then
        assertThatThrownBy(() -> service.rejectedByAdmin(10L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("거절되지 않은 예약 대기 목록을 반환한다.")
    void given_when_findNotRejectedWaitingList_then_returnWaitingResponse() {
        //when, then
        assertThat(service.findNotRejectedWaitingList()).hasSize(3);
    }
}
