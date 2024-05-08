package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import roomescape.controller.request.ReservationTimeRequest;
import roomescape.controller.response.IsReservedTimeResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationDao;
import roomescape.repository.ReservationTimeDao;

@Service
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;

    private final ReservationDao reservationDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeDao.findAllReservationTimes();
    }

    public ReservationTime addReservationTime(ReservationTimeRequest request) {
        LocalTime startAt = request.startAt();
        validateExistTime(startAt);
        ReservationTime reservationTime = new ReservationTime(startAt);
        return reservationTimeDao.addReservationTime(reservationTime);
    }

    private void validateExistTime(LocalTime startAt) {
        long countReservationTimeByStartAt = reservationTimeDao.countReservationTimeByStartAt(startAt);
        if (countReservationTimeByStartAt > 0) {
            throw new DuplicatedException("이미 존재하는 시간입니다.");
        }
    }

    public ReservationTime findReservationTime(long id) {
        return reservationTimeDao.findReservationById(id);
    }

    public List<IsReservedTimeResponse> getIsReservedTime(LocalDate date, long themeId) {
        List<ReservationTime> allTimes = reservationTimeDao.findAllReservationTimes();
        List<ReservationTime> bookedTimes = reservationTimeDao.findAllReservedTimes(date, themeId);
        List<ReservationTime> notBookedTimes = filterNotBookedTimes(allTimes, bookedTimes);
        List<IsReservedTimeResponse> bookedResponse = mapToResponse(bookedTimes, true);
        List<IsReservedTimeResponse> notBookedResponse = mapToResponse(notBookedTimes, false);
        return concat(notBookedResponse, bookedResponse);
    }

    public void deleteReservationTime(long id) {
        validateNotExistReservationTime(id);
        validateReservedTime(id);
        reservationTimeDao.deleteReservationTime(id);
    }

    private void validateReservedTime(long id) {
        long countedReservationByTime = reservationDao.countReservationByTimeId(id);
        if (countedReservationByTime > 0) {
            throw new BadRequestException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }

    private void validateNotExistReservationTime(long id) {
        long countedReservationTime = reservationTimeDao.countReservationTimeById(id);
        if (countedReservationTime <= 0) {
            throw new NotFoundException("id(%s)에 해당하는 예약 시간이 존재하지 않습니다.".formatted(id));
        }
    }

    private List<ReservationTime> filterNotBookedTimes(List<ReservationTime> times, List<ReservationTime> bookedTimes) {
        return times.stream()
                .filter(time -> !bookedTimes.contains(time))
                .toList();
    }

    private List<IsReservedTimeResponse> mapToResponse(List<ReservationTime> times, boolean isBooked) {
        return times.stream()
                .map(time -> new IsReservedTimeResponse(time.getId(), time.getStartAt(), isBooked))
                .toList();
    }

    private List<IsReservedTimeResponse> concat(List<IsReservedTimeResponse> notBookedTimes,
                                                List<IsReservedTimeResponse> bookedTimes) {
        return Stream.concat(notBookedTimes.stream(), bookedTimes.stream()).toList();
    }
}
