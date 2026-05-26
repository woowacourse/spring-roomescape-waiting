package roomescape.service;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.HiddenResourceException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dao.ReservationDao;
import roomescape.domain.Reservation;

@Service
@Transactional(readOnly = true)
public class ReservationAuthorizationService {
    private final ReservationDao reservationDao;

    public ReservationAuthorizationService(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public void validateMemberCanAccess(Long memberId, Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (!reservation.getMember().getId().equals(memberId)) {
            throw new HiddenResourceException();
        }
    }

    public void validateManagerCanAccess(Long storeId, Long reservationId) {
        if (storeId == null) {
            throw new UnauthorizedException();
        }
        Reservation reservation = findReservation(reservationId);
        if (!Objects.equals(storeId, reservation.getStoreId())) {
            throw new UnauthorizedException();
        }
    }

    private Reservation findReservation(Long reservationId) {
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
    }
}
