package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BadRequestException;
import roomescape.reservation.domain.entity.MemberReservation;
import roomescape.reservation.domain.entity.ReservationStatus;
import roomescape.reservation.domain.service.WaitingReservationService;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.repository.MemberReservationRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(value = {WaitingReservationService.class})
@Sql(value = "/recreate_table.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DataJpaTest
@DisplayName("예약 대기 서비스")
class WaitingReservationServiceTest {

    private final WaitingReservationService waitingReservationService;
    private final MemberReservationRepository memberReservationRepository;

    @Autowired
    public WaitingReservationServiceTest(WaitingReservationService waitingReservationService,
                                         MemberReservationRepository memberReservationRepository) {
        this.waitingReservationService = waitingReservationService;
        this.memberReservationRepository = memberReservationRepository;
    }

    @DisplayName("예약 대기 서비스는 예약 대기 목록을 조회한다")
    @Test
    void readWaitingReservations() {
        // when
        List<MemberReservationResponse> reservationResponses = waitingReservationService.readWaitingReservations();

        // then
        assertThat(reservationResponses).hasSize(3);
    }

    @DisplayName("예약 대기 서비스는 예약 대기를 승인한다")
    @Test
    void confirmWaitingReservation() {
        // given
        Long id = 11L;

        // when
        waitingReservationService.confirmWaitingReservation(id);
        Optional<MemberReservation> actual = memberReservationRepository.findById(id);

        // then
        assertThat(actual.get().getStatus()).isEqualTo(ReservationStatus.CONFIRMATION);
    }

    @DisplayName("예약 대기 서비스는 대기 상태가 아닌 예약을 승인하려고하면 예외가 발생한다.")
    @Test
    void confirmNotWaitingReservation() {
        // given
        Long id = 1L;

        // when & then
        assertThatThrownBy(() -> waitingReservationService.confirmWaitingReservation(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 예약은 대기 상태가 아닙니다.");
    }

    @DisplayName("예약 대기 서비스는 첫 번째 예약 대기가 아닌 대기를 승인하려고 하면 예외가 발생한다.")
    @Test
    void confirmNotFirstWaitingReservation() {
        // given
        Long id = 12L;

        // when & then
        assertThatThrownBy(() -> waitingReservationService.confirmWaitingReservation(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("예약 대기는 순서대로 승인할 수 있습니다.");
    }

    @DisplayName("예약 대기 서비스는 이미 예약이 존재하는 대기를 승인하려고 하는 경우 예외가 발생한다.")
    @Test
    void confirmAlreadyConfirmReservation() {
        // given
        Long id = 14L;

        // when & then
        assertThatThrownBy(() -> waitingReservationService.confirmWaitingReservation(id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 예약이 존재해 대기를 승인할 수 없습니다.");
    }
}
