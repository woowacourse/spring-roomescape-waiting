package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.ReservationTimeCreateRequest;
import roomescape.dto.response.ReservationTimeWithIsBookedGetResponse;
import roomescape.exception.DuplicateTimeException;
import roomescape.exception.InvalidInputException;

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
