package roomescape.reservation.service;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.BadRequestException;
import roomescape.exception.UnauthorizedException;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.repository.ReservationTimeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@EnableJpaAuditing
@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationServiceRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    MemberRepository memberRepository;
    ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
                memberRepository);
    }

    @DisplayName("예약 대기를 생성할 수 있다.")
    @Test
    void createWaiting() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(
                1L,
                LocalDate.now().plusMonths(6),
                1L,
                1L);
        reservationService.createReservation(request);

        //when
        WaitingResponse waitingReservation = reservationService.createWaitingReservation(request);

        //then
        assertThat(waitingReservation.status()).isEqualTo("대기");
    }

    @DisplayName("기존 예약이 없으면 대기를 생성할 수 없다.")
    @Test
    void createWaitingWithoutReservation() {
        ReservationCreateRequest request = new ReservationCreateRequest(
                1L,
                LocalDate.now().plusMonths(6),
                1L,
                1L);
        assertThatThrownBy(() -> reservationService.createWaitingReservation(request))
                .isInstanceOf(BadRequestException.class);
    }

    @DisplayName("예약 대기를 승인할 수 있다.")
    @Test
    void approveWaiting() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(
                1L,
                LocalDate.now().plusMonths(6),
                1L,
                1L);
        Long reservationId = reservationService.createReservation(request).id();
        Long waitingId = reservationService.createWaitingReservation(request).id();
        reservationService.deleteReservation(reservationId);

        //when
        ReservationResponse reservationResponse = reservationService.approveWaiting(waitingId);

        //then
        assertThat(reservationResponse.id()).isEqualTo(waitingId);
        assertThat(reservationResponse.status()).isEqualTo("예약");
    }

    @DisplayName("기존 예약이 있으면 예약 대기를 승인할 수 없다.")
    @Test
    void approveWaitingWhenReservationExist() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(
                1L,
                LocalDate.now().plusMonths(6),
                1L,
                1L);
        reservationService.createReservation(request);
        Long waitingId = reservationService.createWaitingReservation(request).id();

        //when, then
        assertThatThrownBy(() -> reservationService.approveWaiting(waitingId))
                .isInstanceOf(BadRequestException.class);
    }

    @DisplayName("예약 대기를 삭제할 수 있다.")
    @Test
    void deleteWaiting() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(
                1L,
                LocalDate.now().plusMonths(6),
                1L,
                1L);
        reservationService.createReservation(request);
        Long waitingId = reservationService.createWaitingReservation(request).id();

        //when, then
        assertThatCode(() -> reservationService.deleteWaiting(waitingId))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 대기가 아닌 예약은 deleteWaiting()으로 삭제할 수 없다.")
    @Test
    void deleteReservationAsDeleteWaiting() {
        //given
        ReservationCreateRequest request = new ReservationCreateRequest(
                1L,
                LocalDate.now().plusMonths(6),
                1L,
                1L);
        Long reservationId = reservationService.createReservation(request).id();

        //when, then
        assertThatThrownBy(() -> reservationService.deleteWaiting(reservationId))
                .isInstanceOf(UnauthorizedException.class);
    }
}
