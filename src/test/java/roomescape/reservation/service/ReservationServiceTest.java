package roomescape.reservation.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.Fixtures;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.ForbiddenException;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(value = {ReservationService.class, ReservationCreateServiceTest.class})
@Sql(value = "/recreate_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("예약 서비스")
class ReservationServiceTest {

    private final ReservationService reservationService;

    private final Long id;
    private final String name;
    private final LocalDate date;

    @Autowired
    public ReservationServiceTest(ReservationService reservationService) {
        this.reservationService = reservationService;
        this.id = 1L;
        this.name = "클로버";
        this.date = LocalDate.now().plusMonths(6);
    }

    @DisplayName("예약 서비스는 예약 확정 상태인 예약들을 조회한다.")
    @Test
    void readReservations() {
        // when
        List<MemberReservationResponse> reservations = reservationService.readReservations();

        // then
        assertThat(reservations.size()).isEqualTo(10);
    }

    @DisplayName("예약 서비스는 id에 맞는 예약을 조회한다.")
    @Test
    void readReservation() {
        // when
        Long id = 1L;
        MemberReservationResponse reservation = reservationService.readReservation(id);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(LocalDate.of(2099, 12, 31));
        softAssertions.assertThat(reservation.memberName()).isEqualTo("클로버");
        softAssertions.assertAll();
    }

    @DisplayName("예약 서비스는 예약을 생성한다.")
    @Test
    void createReservation() {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(1L, date, 1L, 1L);

        // when
        MemberReservationResponse reservation = reservationService.createReservation(request);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(date);
        softAssertions.assertThat(reservation.memberName()).isEqualTo(name);
        softAssertions.assertThat(reservation.startAt()).isEqualTo(LocalTime.of(10, 0));
        softAssertions.assertAll();
    }

    @DisplayName("예약 서비스는 사용자 예약을 생성한다.")
    @Test
    void createMemberReservation() {
        // given
        MemberReservationCreateRequest request = new MemberReservationCreateRequest(date, 1L, 1L, ReservationStatus.CONFIRMATION);
        LoginMember loginMember = Fixtures.loginMemberFixture;

        // when
        MemberReservationResponse reservation = reservationService.createReservation(request, loginMember);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(date);
        softAssertions.assertThat(reservation.memberName()).isEqualTo(name);
        softAssertions.assertThat(reservation.startAt()).isEqualTo(LocalTime.of(10, 0));
        softAssertions.assertAll();
    }

    @DisplayName("예약 서비스는 사용자 예약 대기를 생성한다.")
    @Test
    void createWaitingReservation() {
        // given
        MemberReservationCreateRequest request = new MemberReservationCreateRequest(date, 1L, 1L, ReservationStatus.WAITING);
        LoginMember loginMember = Fixtures.loginMemberFixture;

        // when
        MemberReservationResponse reservation = reservationService.createReservation(request, loginMember);

        // then
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(reservation.date()).isEqualTo(date);
        softAssertions.assertThat(reservation.memberName()).isEqualTo(name);
        softAssertions.assertThat(reservation.startAt()).isEqualTo(LocalTime.of(10, 0));
        softAssertions.assertAll();
    }

    @DisplayName("예약 서비스는 id에 맞는 예약을 삭제한다.")
    @Test
    void deleteReservation() {
        // when & then
        assertThatCode(() -> reservationService.deleteReservation(id))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 서비스는 예약 삭제를 요청한 사용자가 예약의 주인이 아닌 경우 예외가 발생한다.")
    @Test
    void deleteNotOwnerReservation() {
        // given
        Long id = 1L;
        LoginMember loginMember = new LoginMember(3L, "admin@gamil.com");

        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(id, loginMember))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("본인의 예약 대기만 삭제할 수 있습니다.");
    }
}
