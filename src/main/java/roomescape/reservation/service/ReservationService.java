package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.MyReservationWithStatus;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationSlotRepository;

import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final CommonFindService commonFindService;
    private final ReservationSlotRepository reservationSlotRepository;
    private final MemberReservationRepository memberReservationRepository;

    public ReservationService(CommonFindService commonFindService,
                              ReservationSlotRepository reservationSlotRepository,
                              MemberReservationRepository memberReservationRepository) {
        this.commonFindService = commonFindService;
        this.reservationSlotRepository = reservationSlotRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    @Transactional(readOnly = true)
    public List<MyReservationWithStatus> findMyReservations(AuthInfo authInfo) {
        Member member = commonFindService.getMember(authInfo.getId());
        return memberReservationRepository.findAllByMember(member)
                .stream()
                .map(MyReservationWithStatus::from)
                .toList();
    }

    public void delete(long reservationId) {
        memberReservationRepository.deleteByReservationSlot_Id(reservationId);
        reservationSlotRepository.deleteById(reservationId);
    }
}
