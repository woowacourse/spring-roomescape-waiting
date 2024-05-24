package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.MyReservationWithStatus;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationSlotRepository;

import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final CommonFindService commonFindService;
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(CommonFindService commonFindService,
                              ReservationSlotRepository reservationSlotRepository,
                              ReservationRepository reservationRepository) {
        this.commonFindService = commonFindService;
        this.reservationSlotRepository = reservationSlotRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<MyReservationWithStatus> findMyReservations(AuthInfo authInfo) {
        Member member = commonFindService.getMember(authInfo.getId());
        return reservationRepository.findAllByMember(member)
                .stream()
                .map(MyReservationWithStatus::from)
                .toList();
    }

    public void delete(long reservationId) {
        reservationRepository.deleteByReservationSlot_Id(reservationId);
        reservationSlotRepository.deleteById(reservationId);
    }
}
