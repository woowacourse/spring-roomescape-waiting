package roomescape.reservation.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.ForbiddenException;
import roomescape.reservation.domain.entity.MemberReservation;
import roomescape.reservation.domain.entity.ReservationStatus;
import roomescape.reservation.domain.service.ReservationService;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.repository.MemberReservationRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(value = {ReservationService.class})
@Sql(value = "/recreate_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("예약 서비스")
class ReservationServiceTest {

    private final ReservationService reservationService;
    private final MemberReservationRepository memberReservationRepository;

    private final Long id;

    @Autowired
    public ReservationServiceTest(ReservationService reservationService,
                                  MemberReservationRepository memberReservationRepository) {
        this.reservationService = reservationService;
        this.memberReservationRepository = memberReservationRepository;
        this.id = 1L;
    }

    @DisplayName("예약 서비스는 예약 확정 상태인 예약들을 조회한다.")
    @Test
    void readReservations() {
        // when
        List<MemberReservationResponse> reservations = reservationService.readReservations();

        // then
        assertThat(reservations.size()).isEqualTo(11);
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

    @DisplayName("예약 서비스는 다음 대기가 있는 경우 예약 삭제 시 대기를 자동으로 승인한다.")
    @Test
    void confirmFirstWaitingReservation() {
        // given
        Long deleteId = 13L;
        Long firstWaitingId = 14L;
        MemberReservation firstWaiting = memberReservationRepository.findById(firstWaitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 예약입니다."));

        // when
        reservationService.deleteReservation(deleteId);

        // then
        assertThat(firstWaiting.getStatus()).isEqualTo(ReservationStatus.CONFIRMATION);
    }
}
