package roomescape.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.HiddenResourceException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.reservation.ReservationDao;
import roomescape.domain.member.Member;
import roomescape.reservation.Reservation;
import roomescape.domain.store.Store;

@Service
@Transactional(readOnly = true)
public class ReservationAuthorizationService {
    private final ReservationDao reservationDao;

    public ReservationAuthorizationService(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public void validateMemberCanAccess(Member member, Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (!reservation.isSameMember(member)) {
            throw new HiddenResourceException();
        }
    }

    public void validateManagerCanAccess(Member manager, Long reservationId) {
        Store store = manager.getStore();
        if (store == null) {
            throw new UnauthorizedException();
        }
        Reservation reservation = findReservation(reservationId);
        if (!reservation.isInStore(store)) {
            throw new UnauthorizedException();
        }
    }

    private Reservation findReservation(Long reservationId) {
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
    }
}
