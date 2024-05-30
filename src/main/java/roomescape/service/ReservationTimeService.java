package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import roomescape.controller.request.ReservationTimeRequest;
import roomescape.controller.response.IsReservedTimeResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final Logger logger = LoggerFactory.getLogger(ReservationTimeService.class);

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }


    public List<ReservationTime> findAllReservationTimes() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime findReservationTime(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("아이디에 해당하는 예약 시간이 존재하지 않습니다. : 예약 시간 아이디={}", id);
                    return new NotFoundException("예약 시간", id);
                });
    }

    public ReservationTime addReservationTime(ReservationTimeRequest request) {
        LocalTime startAt = request.startAt();
        validateExistTime(startAt);
        ReservationTime reservationTime = new ReservationTime(startAt);
        return reservationTimeRepository.save(reservationTime);
    }

    public List<IsReservedTimeResponse> getIsReservedTime(LocalDate date, long themeId) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        List<ReservationTime> bookedTimes = reservationTimeRepository.findAllReservedTimes(date, themeId);
        List<ReservationTime> notBookedTimes = filterNotBookedTimes(allTimes, bookedTimes);
        List<IsReservedTimeResponse> bookedResponse = mapToResponse(bookedTimes, true);
        List<IsReservedTimeResponse> notBookedResponse = mapToResponse(notBookedTimes, false);
        return concat(notBookedResponse, bookedResponse);
    }

    public void deleteReservationTime(long id) {
        validateNotExistReservationTime(id);
        validateReservedTime(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateExistTime(LocalTime startAt) {
        long countReservationTimeByStartAt = reservationTimeRepository.countByStartAt(startAt);
        if (countReservationTimeByStartAt > 0) {
            logger.error("이미 존재하는 시간을 조회했습니다 : 조회한 시간={}", startAt);
            throw new DuplicatedException("시간");
        }
    }

    private void validateReservedTime(long id) {
        ReservationTime time = findReservationTime(id);
        long countedReservationByTime = reservationRepository.countByTime(time);
        if (countedReservationByTime > 0) {
            logger.error("삭제하려는 사간으로 예약한 예약내역이 존재하여 시간을 삭제할 수 없습니다 : 예약 시간 아이디={}", id);
            throw new BadRequestException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }

    private void validateNotExistReservationTime(long id) {
        long countedReservationTime = reservationTimeRepository.countById(id);
        if (countedReservationTime <= 0) {
            logger.error("아이디에 해당하는 예약 시간이 존재하지 않습니다 : 예약 시간 아이디={}", id);
            throw new NotFoundException("예약 시간", id);
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
