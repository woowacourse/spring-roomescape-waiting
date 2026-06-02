package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ReservationAdminCommandService {

    private final ReservationDao reservationDao;

    @Transactional
    public void delete(Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("삭제하려는 예약이 존재하지 않습니다."));

        reservationDao.delete(reservation);
    }
}
