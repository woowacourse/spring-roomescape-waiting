package roomescape.reservation.service.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.service.dto.MyReservationInfo;

@Service
@Transactional(readOnly = true)
public class MemberReservationService {

    private final ReservationRepository reservationRepository;

    private final MemberReservationRepository memberReservationRepository;

    public MemberReservationService(ReservationRepository reservationRepository,
                                    MemberReservationRepository memberReservationRepository) {
        this.reservationRepository = reservationRepository;
        this.memberReservationRepository = memberReservationRepository;
    }


    public List<ReservationResponse> findMemberReservations(ReservationQueryRequest request) {
        return memberReservationRepository.findBy(
                        request.getMemberId(),
                        request.getThemeId(),
                        ReservationStatus.APPROVED,
                        request.getStartDate(),
                        request.getEndDate())
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationInfo> findMyReservations(Member member) {
        return memberReservationRepository.findByMember(member.getId())
                .stream()
                .map(MyReservationInfo::of)
                .toList();
    }

    @Transactional
    public MemberReservation createMemberReservation(Member member, Reservation reservation) {
        return memberReservationRepository.save(
                new MemberReservation(member, reservation, ReservationStatus.APPROVED));
    }

    @Transactional
    public void updateStatus(MemberReservation memberReservation, ReservationStatus from, ReservationStatus to) {
        memberReservationRepository.updateStatusBy(to, memberReservation.getReservation(), from, 1);
    }

    @Transactional
    public void delete(long reservationId) {
        memberReservationRepository.deleteByReservationId(reservationId);
        reservationRepository.deleteById(reservationId);
    }
}
