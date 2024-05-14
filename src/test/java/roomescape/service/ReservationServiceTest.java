package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.exception.InvalidClientFieldWithValueException;
import roomescape.exception.ReservationFailException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationServiceTest {
    private final ReservationService service;

    @Autowired
    public ReservationServiceTest(final ReservationService service) {
        this.service = service;
    }

    private long getReservationSize() {
        return service.findEntireReservationList().getData().size();
    }

    @Test
    @DisplayName("예약 목록을 반환한다.")
    void given_when_findEntireReservationList_then_returnReservationResponses() {
        //when, then
        assertThat(service.findEntireReservationList().getData().size()).isEqualTo(7);
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
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 1L, 99L, 1L);
        //when, then
        assertThatThrownBy(() -> service.create(reservationRequest)).isInstanceOf(InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("timeId 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_createWithNotExistTimeId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 99L, 1L, 1L);
        //when, then
        assertThatThrownBy(() -> service.create(reservationRequest)).isInstanceOf(InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("memberId 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_createWithNotExistMemberId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 1L, 1L, 99L);
        //when, then
        assertThatThrownBy(() -> service.create(reservationRequest)).isInstanceOf(InvalidClientFieldWithValueException.class);
        assertThat(getReservationSize()).isEqualTo(initialSize);
    }
}