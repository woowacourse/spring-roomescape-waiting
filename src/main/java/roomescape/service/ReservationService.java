package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservation.ReservationExistenceCheck;
import roomescape.dto.reservation.ReservationFilterParam;
import roomescape.dto.reservation.ReservationResponse;

import java.util.List;

@Service
@Transactional
public class ReservationService {

    private static final int MAX_RESERVATIONS_PER_TIME = 1;

    private final ReservationDao reservationDao;

    public ReservationService(final ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public ReservationResponse create(final Reservation reservation) {
        final ReservationExistenceCheck reservationExistenceCheck
                = new ReservationExistenceCheck(reservation.getDate(), reservation.getReservationTimeId(), reservation.getThemeId());
        final List<Reservation> existingReservations = reservationDao.findAllBy(reservationExistenceCheck);
        validateDuplicatedReservation(existingReservations);
        return ReservationResponse.from(reservationDao.save(reservation));
    }

    private void validateDuplicatedReservation(final List<Reservation> existingReservations) {
        if (existingReservations.size() >= MAX_RESERVATIONS_PER_TIME) {
            throw new IllegalArgumentException("해당 시간대에 예약이 모두 찼습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        final List<Reservation> reservations = reservationDao.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllBy(final ReservationFilterParam reservationFilterParam) {
        final List<Reservation> reservations = reservationDao.findAllBy(reservationFilterParam);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void delete(final Long id) {
        final boolean isExist = reservationDao.existById(id);
        if (!isExist) {
            throw new IllegalArgumentException("해당 ID의 예약이 없습니다.");
        }
        reservationDao.deleteById(id);
    }
}
