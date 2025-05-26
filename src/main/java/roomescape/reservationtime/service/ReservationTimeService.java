package roomescape.reservationtime.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponseWithBookedStatus;
import roomescape.reservationtime.repository.ReservationTimeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;

    @Autowired
    public ReservationTimeService(
            ReservationTimeRepository reservationTimeRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Transactional
    public ReservationTimeResponse createReservationTime(ReservationTimeCreateRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.save(request.toReservationTime());
        return ReservationTimeResponse.from(reservationTime);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return convertTimesToReservationTimeResponses(reservationTimes);
    }

    @Transactional
    public void deleteReservationTimeById(Long id) {
        reservationTimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponseWithBookedStatus> findAvailableReservationTimesByDateAndThemeId(
            LocalDate date,
            Long themeId
    ) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        List<ReservationTime> availableTimes = reservationTimeRepository.findByReservationDateAndThemeId(date, themeId);

        return allTimes.stream()
                .map(time ->
                        ReservationTimeResponseWithBookedStatus.of(time, !availableTimes.contains(time))
                ).toList();
    }

    private List<ReservationTimeResponse> convertTimesToReservationTimeResponses(List<ReservationTime> times) {
        return times.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationTime getReservationTimeByTimeId(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("올바른 예약 시간을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findByReservationDateAndThemeId(LocalDate date, Long themeId) {
        return reservationTimeRepository.findByReservationDateAndThemeId(date, themeId);
    }
}
