package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.TestDataInitExtension;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.Reservation;
import roomescape.domain.Role;
import roomescape.domain.Waiting;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.exception.ReservationFailException;
import roomescape.exception.clienterror.InvalidClientFieldWithValueException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(TestDataInitExtension.class)
class ReservationServiceTest {
    private final ReservationService service;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    @Autowired
    public ReservationServiceTest(final ReservationService service, final ReservationRepository reservationRepository,
                                  final WaitingRepository waitingRepository) {
        this.service = service;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    private long getReservationSize() {
        return service.findEntireReservationList().getData().size();
    }

    @Test
    @DisplayName("예약 목록을 반환한다.")
    void given_when_findEntireReservationList_then_returnReservationResponses() {
        //when, then
        assertThat(service.findEntireReservationList().getData().size()).isEqualTo(10);
    }

    @Test
    @DisplayName("예약이 성공하면 결과값과 함께 Db에 저장된다.")
    void given_reservationRequestWithInitialSize_when_createReservation_then_returnReservationResponseAndSaveDb() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2999-01-01"), 1L, 1L, 1L);
        //when
        final ReservationResponse reservationResponse = service.create(reservationRequest);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(reservationResponse.id()).isEqualTo(afterCreateSize);
        assertThat(afterCreateSize).isEqualTo(initialSize + 1);
    }

    @Test
    @DisplayName("존재하는 예약을 삭제하면 Db에도 삭제된다.")
    void given_initialSize_when_delete_then_deletedItemInDb() {
        //given
        long initialSize = getReservationSize();
        //when
        service.delete(7L);
        long afterCreateSize = getReservationSize();
        //then
        assertThat(afterCreateSize).isEqualTo(initialSize - 1);
    }

    /*
     * 예약 대기 관련 초기 데이터
     * 예약 테이블
     * {ID=9, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=1, STATUS=RESERVED}
     * {ID=10, DATE=내일일자, TIME_ID=2, THEME_ID=1, MEMBER_ID=1, STATUS=RESERVED}
     * 예약 대기 테이블
     * {ID=2, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
     */
    @Test
    @DisplayName("예약 삭제 시 대기중인 내역이 있으면 해당 대기를 삭제하고 예약으로 저장한다.")
    void given_when_existWaiting_then_convertWaitingToReservation() {
        //given
        Reservation reservationToCancel = reservationRepository.findById(9L).get();
        Waiting waitingToApprove = waitingRepository.findById(2L).get();

        //when
        service.delete(9L);

        //then
        Optional<Reservation> approvedReservation = reservationRepository.findById(11L);
        assertAll(
                () -> assertThat(waitingRepository.findById(2L)).isEmpty(),
                () -> assertThat(approvedReservation).isNotEmpty(),
                () -> assertThat(approvedReservation.get().getTheme()).isEqualTo(reservationToCancel.getTheme()),
                () -> assertThat(approvedReservation.get().getDate()).isEqualTo(reservationToCancel.getDate()),
                () -> assertThat(approvedReservation.get().getMember()).isEqualTo(waitingToApprove.getMember())
        );
    }

    @Test
    @DisplayName("이전 날짜로 예약 할 경우 예외가 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_createWithPastDate_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("1999-01-01"), 1L, 1L, 1L);
        //when, then
        assertThatThrownBy(() -> service.create(reservationRequest)).isInstanceOf(ReservationFailException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("themeId가 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_createWithNotExistThemeId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 1L, 99L,
                1L);
        //when, then
        assertThatThrownBy(() -> service.create(reservationRequest)).isInstanceOf(
                InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("timeId 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_createWithNotExistTimeId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 99L, 1L,
                1L);
        //when, then
        assertThatThrownBy(() -> service.create(reservationRequest)).isInstanceOf(
                InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("memberId 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_createWithNotExistMemberId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 1L, 1L,
                99L);
        //when, then
        assertThatThrownBy(() -> service.create(reservationRequest)).isInstanceOf(
                InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("로그인한 회원의 예약 목록을 반환한다.")
    void given_member_when_findReservationByMember_then_returnReservationMineResponses() {
        //given
        Password password = new Password("hashedpassword", "salt");
        Member member = new Member(1L, "user@test.com", password, "poke", Role.USER);
        //when
        final List<ReservationsMineResponse> reservationsByMember = service.findReservationsByMember(member);
        //then
        assertThat(reservationsByMember).hasSize(9);
    }

    @Test
    @DisplayName("지나간 날짜의 예약을 생성 시 예외가 발생한다.")
    void given_reservationRequestWithPastDate_when_save_then_throwException() {
        //given
        ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("1999-01-01"), 1L, 1L, 1L);
        //when, then
        assertThatThrownBy(() -> service.create(reservationRequest)).isInstanceOf(ReservationFailException.class);
    }
}
