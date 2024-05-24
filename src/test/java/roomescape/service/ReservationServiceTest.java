package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservation.dto.ReservationListResponse;
import roomescape.service.reservation.dto.ReservationMineListResponse;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

class ReservationServiceTest extends ServiceTest {
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Nested
    @DisplayName("예약 목록 조회")
    class FindAllReservation {
        @Test
        void 필터링_없이_전체_예약_목록을_조회할_수_있다() {
            ReservationListResponse response = reservationService.findAllReservation(
                    null, null, null, null);

            assertThat(response.getReservations().size())
                    .isEqualTo(1);
        }

        @Test
        void 예약_목록을_예약자별로_필터링해_조회할_수_있다() {
            ReservationListResponse response = reservationService.findAllReservation(
                    1L, null, null, null);

            assertThat(response.getReservations().size())
                    .isEqualTo(1);
        }

        @Test
        void 예약_목록을_테마별로_필터링해_조회할_수_있다() {
            ReservationListResponse response = reservationService.findAllReservation(
                    null, 1L, null, null);

            assertThat(response.getReservations().size())
                    .isEqualTo(1);
        }

        @Test
        void 예약_목록을_기간별로_필터링해_조회할_수_있다() {
            LocalDate dateFrom = LocalDate.of(2000, 4, 1);
            LocalDate dateTo = LocalDate.of(2000, 4, 3);
            ReservationListResponse response = reservationService.findAllReservation(
                    null, null, dateFrom, dateTo);

            assertThat(response.getReservations().size())
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("내 예약 목록 조회")
    class FindMyReservation {
        @Test
        void 내_예약_목록을_조회할_수_있다() {
            Member member = memberRepository.findById(1L).orElseThrow();

            ReservationMineListResponse response = reservationService.findMyReservation(member);

            assertThat(response.getReservations().size())
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("예약 추가")
    class SaveReservation {
        @Test
        void 예약을_추가할_수_있다() {
            ReservationRequest request = new ReservationRequest("2000-04-07", "1", "1");
            Member member = memberRepository.findById(1L).orElseThrow();

            ReservationResponse response = reservationService.saveReservation(request, member);

            assertThat(response.getName())
                    .isEqualTo(member.getName().getName());
        }

        @Test
        void 시간대와_테마가_똑같은_중복된_예약_추가시_예외가_발생한다() {
            ReservationRequest request = new ReservationRequest("2000-04-01", "1", "1");
            Member member = memberRepository.findById(1L).orElseThrow();

            assertThatThrownBy(() -> reservationService.saveReservation(request, member))
                    .isInstanceOf(DuplicatedReservationException.class);
        }

        @Test
        void 지나간_날짜와_시간에_대한_예약_추가시_예외가_발생한다() {
            ReservationRequest request = new ReservationRequest("2000-04-06", "1", "1");
            Member member = memberRepository.findById(1L).orElseThrow();

            assertThatThrownBy(() -> reservationService.saveReservation(request, member))
                    .isInstanceOf(InvalidDateTimeReservationException.class);
        }
    }

    @Nested
    @DisplayName("예약 삭제")
    class DeleteReservation {
        @Test
        void 예약을_삭제할_수_있다() {
            ReservationWaiting waiting = reservationWaitingRepository.findById(1L).orElseThrow();
            Reservation reservation = reservationRepository.findById(1L).orElseThrow();
            reservationWaitingRepository.delete(waiting);
            reservationRepository.delete(reservation);

            ReservationListResponse response = reservationService.findAllReservation(null, null, null, null);
            assertThat(response.getReservations().size())
                    .isEqualTo(0);
        }

        @Test
        void 존재하지_않는_예약은_삭제할_수_없다() {
            assertThatThrownBy(() -> reservationService.deleteReservation(10L))
                    .isInstanceOf(NotFoundReservationException.class);
        }
    }
}
