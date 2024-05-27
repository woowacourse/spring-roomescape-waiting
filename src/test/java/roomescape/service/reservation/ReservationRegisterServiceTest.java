package roomescape.service.reservation;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.dto.ReservationRequest;
import roomescape.domain.dto.ReservationResponse;
import roomescape.exception.InvalidClientFieldWithValueException;
import roomescape.exception.ReservationFailException;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationRegisterServiceTest {
    private final ReservationRegisterService service;
    private final ReservationRepository repository;

    @Autowired
    public ReservationRegisterServiceTest(final ReservationRegisterService service, final ReservationRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    private long getReservationSize() {
        return repository.findAll().size();
    }

    @Test
    @DisplayName("예약이 성공하면 결과값과 함께 Db에 저장된다.")
    void given_reservationRequestWithInitialSize_when_register_then_returnReservationResponseAndSaveDb() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2999-01-01"), 1L, 1L, 1L);
        //when
        final ReservationResponse reservationResponse = service.register(reservationRequest);
        long afterCreateSize = getReservationSize();
        //then
        assertAll(
                () -> assertThat(reservationResponse.id()).isEqualTo(afterCreateSize),
                () -> assertThat(afterCreateSize).isEqualTo(initialSize + 1)
        );
    }

    @Test
    @DisplayName("이전 날짜로 예약 할 경우 예외가 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_registerWithPastDate_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("1999-01-01"), 1L, 1L, 1L);
        //when, then
        assertAll(
                () -> assertThatThrownBy(() -> service.register(reservationRequest)).isInstanceOf(ReservationFailException.class),
                () -> assertThat(getReservationSize()).isEqualTo(initialSize)
        );
    }

    @Test
    @DisplayName("themeId가 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_registerWithNotExistThemeId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 1L, 99L,
                1L);
        //when, then
        assertAll(
                () -> assertThatThrownBy(() -> service.register(reservationRequest)).isInstanceOf(
                        InvalidClientFieldWithValueException.class),
                () -> assertThat(getReservationSize()).isEqualTo(initialSize)
        );
    }

    @Test
    @DisplayName("timeId 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_registerWithNotExistTimeId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 99L, 1L,
                1L);
        //when, then
        assertAll(
                () -> assertThatThrownBy(() -> service.register(reservationRequest)).isInstanceOf(
                        InvalidClientFieldWithValueException.class),
                () -> assertThat(getReservationSize()).isEqualTo(initialSize)
        );
    }

    @Test
    @DisplayName("memberId 존재하지 않을 경우 예외를 발생하고, Db에 저장하지 않는다.")
    void given_reservationRequestWithInitialSize_when_registerWithNotExistMemberId_then_throwException() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-01-01"), 1L, 1L,
                99L);
        //when, then
        assertAll(
                () -> assertThatThrownBy(() -> service.register(reservationRequest)).isInstanceOf(
                        InvalidClientFieldWithValueException.class),
                () -> assertThat(getReservationSize()).isEqualTo(initialSize)
        );
    }

    @Test
    @DisplayName("이미 예약이 되어있는 날짜와 시간 및 테마에 다른 사용자가 예약 등록을 할 경우 예약이 저장된다.")
    void given_reservationRequest_when_registerAlreadyReservedWithDifferentMemberId_then_createdWithStatusIsWaiting() {
        //given
        long initialSize = getReservationSize();
        final ReservationRequest reservationRequest = new ReservationRequest(LocalDate.parse("2099-04-30"), 1L, 1L, 2L);
        //when
        final ReservationResponse reservationResponse = service.register(reservationRequest);
        long afterCreateSize = getReservationSize();
        //then
        assertAll(
                () -> assertThat(reservationResponse.id()).isEqualTo(afterCreateSize),
                () -> assertThat(afterCreateSize).isEqualTo(initialSize + 1)
        );
    }
}