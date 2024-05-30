package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.Reservation;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.helper.fixture.DateFixture;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.ReservationMineResponse;
import roomescape.service.dto.response.ReservationResponse;

class ReservationServiceTest extends ServiceTest{

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 조건에_해당하는_모든_예약을_조회한다() {
        List<ReservationResponse> reservations = reservationService.findAllReservation(
                1L,
                1L,
                DateFixture.today(),
                DateFixture.dayAfterTomorrow()
        );

        assertThat(reservations.size()).isEqualTo(1);
    }

    @Test
    void 내가_작성한_모든_예약을_조회한다() {
        Member member = memberRepository.findById(2L).get();
        List<ReservationMineResponse> reservations = reservationService.findMyReservation(member);

        assertThat(reservations.size()).isEqualTo(2);
    }

    @Test
    void 예약을_저장한다() {
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusYears(1), 1L, 1L);
        Member member = memberRepository.findById(1L).get();

        ReservationResponse response = reservationService.saveReservation(request, member);
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void 중복된_예약은_할_수_없다() {
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusYears(1), 1L, 1L);
        Member member1 = memberRepository.findById(1L).get();
        Member member2 = memberRepository.findById(1L).get();
        reservationService.saveReservation(request, member1);
        assertThatThrownBy(() -> reservationService.saveReservation(request, member2))
                .isInstanceOf(DuplicatedReservationException.class);
    }

    @Test
    void 예약을_삭제할_수_있다() {
        reservationService.deleteReservation(1);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        assertThat(count).isEqualTo(1);
    }
}