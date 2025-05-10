package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.ReservationTimeWithIsBookedGetResponse;
import roomescape.reservation.exception.DuplicateTimeException;
import roomescape.global.exception.InvalidInputException;

@Service
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public ReservationTime createReservationTime(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        LocalTime startAt = reservationTimeCreateRequest.startAt();
        validateDuplicateStartAt(startAt);
        return reservationTimeDao.add(new ReservationTime(null, startAt));
    }

    private void validateDuplicateStartAt(LocalTime startAt) {
        if (reservationTimeDao.existByStartAt(startAt)) {
            throw new DuplicateTimeException();
        }
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeDao.findAll();
    }

    public List<ReservationTimeWithIsBookedGetResponse> findReservationTimeByDateAndThemeIdWithIsBooked(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();
        List<ReservationTime> reservedTimes = findAlreadyReservedTimeWithThemeInDate(date, themeId);
        return reservationTimes.stream()
                .map(reservationTime -> new ReservationTimeWithIsBookedGetResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        reservedTimes.contains(reservationTime)
                ))
                .sorted(Comparator.comparing(ReservationTimeWithIsBookedGetResponse::startAt))
                .toList();
    }

    private List<ReservationTime> findAlreadyReservedTimeWithThemeInDate(LocalDate date, Long themeId) {
        List<Reservation> reservationsWithThemeInDate = reservationDao.findByDateAndThemeId(date, themeId);
        return reservationsWithThemeInDate.stream()
                .map(Reservation::getTime)
                .toList();
    }

    public void deleteReservationTimeById(Long id) {
        if (reservationTimeDao.deleteById(id) == 0) {
            throw new InvalidInputException("존재하지 않는 예약시간 id이다.");
        }
    }
}
