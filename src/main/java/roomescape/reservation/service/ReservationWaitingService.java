package roomescape.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationWaiting;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.reservation.repository.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationDetailRepository detailRepository;

    public ReservationWaitingService(ReservationWaitingRepository waitingRepository,
                                     MemberRepository memberRepository,
                                     ReservationDetailRepository detailRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.detailRepository = detailRepository;
    }

    public List<MyReservationResponse> findReservationWaitingByMemberId(Long id) {
        List<ReservationWaiting> reservationsByMember
                = waitingRepository.findAllByMember_IdOrderByDetailDateAsc(id);
        return reservationsByMember.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findReservationWaitingByConditions(ReservationConditionSearchRequest request) {
        List<ReservationWaiting> reservations = waitingRepository.findAllByMember_Id(request.memberId());

        return reservations.stream()
                .filter(reservation -> reservation.isReservedAtPeriod(request.dateFrom(), request.dateTo()))
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse addReservationWaiting(ReservationRequest reservationRequest) {
        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new BadRequestException("해당 멤버 정보가 존재하지 않습니다."));
        ReservationDetail detail = detailRepository.findById(reservationRequest.detailId())
                .orElseThrow(() -> new BadRequestException("해당 예약 정보가 존재하지 않습니다."));
        validateReservationDetail(detail);

        ReservationWaiting reservation = reservationRequest.toReservationWaiting(member, detail);
        ReservationWaiting savedReservation = waitingRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateReservationDetail(ReservationDetail detail) {
        waitingRepository.findByDetail_Id(detail.getId())
                .ifPresent(reservation -> {
                    throw new ConflictException(
                            "해당 테마(%s)의 해당 시간(%s)에는 이미 예약 대기가 존재합니다."
                                    .formatted(
                                            reservation.getTheme().getName(),
                                            reservation.getTime().getStartAt()));
                });
    }

    public void removeReservations(long id) {
        waitingRepository.deleteById(id);
    }

    public List<ReservationResponse> findReservationWaitings() {
        List<ReservationWaiting> reservations = waitingRepository.findAllByOrderByDetailDateAsc();

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
