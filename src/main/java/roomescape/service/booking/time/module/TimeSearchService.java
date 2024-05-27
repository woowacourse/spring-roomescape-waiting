package roomescape.service.booking.time.module;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.time.ReservationTime;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.reservationtime.TimeWithAvailableResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class TimeSearchService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public TimeSearchService(ReservationTimeRepository reservationTimeRepository,
                             ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse findTime(Long timeId) {
        ReservationTime reservationTime = findTimeById(timeId);
        return ReservationTimeResponse.from(reservationTime);
    }

    public List<ReservationTimeResponse> findAllTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<TimeWithAvailableResponse> findAvailableTimes(LocalDate date, Long themeId) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        return allTimes.stream()
                .map(reservationTime -> createTimeWithAvailableResponses(date, themeId, reservationTime))
                .toList();
    }

    private TimeWithAvailableResponse createTimeWithAvailableResponses(
            LocalDate date, Long themeId, ReservationTime reservationTime) {
        boolean isBooked = reservationRepository.existsByDateAndTimeIdAndThemeId(
                date, reservationTime.getId(), themeId);

        return TimeWithAvailableResponse.from(reservationTime, isBooked);
    }

    private ReservationTime findTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 잘못된 예약시간 정보 입니다.",
                        new Throwable("time_id : " + timeId)
                ));
    }
}
