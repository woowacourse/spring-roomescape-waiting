package roomescape.waiting.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.waiting.domain.WaitingUpdateHistory;
import roomescape.waiting.domain.repository.WaitingUpdateHistoryRepository;

@Service
@Transactional(readOnly = true)
public class WaitingHistoryService {
    private final WaitingUpdateHistoryRepository waitingUpdateHistoryRepository;
    private final MemberReservationRepository memberReservationRepository;


    public WaitingHistoryService(WaitingUpdateHistoryRepository waitingUpdateHistoryRepository,
                                 MemberReservationRepository memberReservationRepository) {
        this.waitingUpdateHistoryRepository = waitingUpdateHistoryRepository;
        this.memberReservationRepository = memberReservationRepository;
    }

    @Transactional
    public void createHistory(Reservation reservation) {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(1);
        waitingUpdateHistoryRepository.save(new WaitingUpdateHistory(reservation, expireTime));
    }

    @Transactional
    public void approveReservationStatus() {
        LocalDateTime now = LocalDateTime.now();
        memberReservationRepository.updateStatusBy(ReservationStatus.APPROVED, ReservationStatus.PENDING,
                1, now.minusHours(2), now);
    }
}
