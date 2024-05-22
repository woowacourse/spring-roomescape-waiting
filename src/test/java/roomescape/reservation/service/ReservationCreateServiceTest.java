package roomescape.reservation.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(value = {ReservationCreateService.class})
@Sql(value = "/recreate_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("예약 생성 서비스")
public class ReservationCreateServiceTest {

    private final ReservationCreateService reservationCreateService;
    private final Long id;
    private final String name;
    private final LocalDate date;

    @Autowired
    public ReservationCreateServiceTest(ReservationCreateService reservationCreateService) {
        this.reservationCreateService = reservationCreateService;
        this.id = 1L;
        this.name = "클로버";
        this.date = LocalDate.now().plusMonths(6);
    }

    @DisplayName("예약 생성 서비스는 예약을 생성한다.")
    @Test
    void createReservation() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(1L, date, 1L, 1L);

        // when
        MemberReservationResponse reservation = reservationCreateService.createReservation(request);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(date);
        softAssertions.assertThat(reservation.memberName()).isEqualTo(name);
        softAssertions.assertThat(reservation.startAt()).isEqualTo(LocalTime.of(10, 0));
        softAssertions.assertAll();
    }

    @DisplayName("예약 생성 서비스는 예약 대기를 생성한다.")
    @Test
    void createWaitingReservation() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(
                1L,
                date,
                1L,
                1L,
                ReservationStatus.WAITING);

        // when
        MemberReservationResponse reservation = reservationCreateService.createReservation(request);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(date);
        softAssertions.assertThat(reservation.memberName()).isEqualTo(name);
        softAssertions.assertThat(reservation.startAt()).isEqualTo(LocalTime.of(10, 0));
        softAssertions.assertAll();
    }

    @DisplayName("예약 생성 서비스는 지난 시점의 예약이 요청되면 예외가 발생한다.")
    @Test
    void validateRequestedTime() {
        // given
        LocalDate date = LocalDate.MIN;
        ReservationCreateRequest request = new ReservationCreateRequest(1L, date, id, id);

        // when & then
        assertThatThrownBy(() -> reservationCreateService.createReservation(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 지난 날짜는 예약할 수 없습니다.");
    }

    @DisplayName("예약 생성 서비스는 중복된 예약 요청이 들어오면 예외가 발생한다.")
    @Test
    void validateIsDuplicated() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(
                1L,
                LocalDate.of(2099, 12, 31),
                1L,
                2L
        );

        // when & then
        assertThatThrownBy(() -> reservationCreateService.createReservation(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 예약한 테마입니다.");
    }

    @DisplayName("예약 생성 서비스는 예약 요청에 존재하지 않는 시간이 포함된 경우 예외가 발생한다.")
    @Test
    void createWithNonExistentTime() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(1L, date, 500L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationCreateService.createReservation(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("존재하지 않는 예약 시간입니다.");
    }

    @DisplayName("예약 생성 서비스는 예약 요청에 존재하지 않는 테마가 포함된 경우 예외가 발생한다.")
    @Test
    void createWithNonExistentTheme() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(
                3L,
                LocalDate.MAX,
                1L,
                500L
        );

        // when & then
        assertThatThrownBy(() -> reservationCreateService.createReservation(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }


    @DisplayName("예약 생성 서비스는 요청받은 테마가 동시간대에 이미 예약된 경우 예외가 발생한다.")
    @Test
    void createWithReservedTheme() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(
                3L,
                LocalDate.of(2024, 12, 1),
                1L,
                2L
        );

        // when & then
        assertThatThrownBy(() -> reservationCreateService.createReservation(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("다른 사용자가 이미 예약한 테마입니다.");
    }
}
