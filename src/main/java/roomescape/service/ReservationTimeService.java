package roomescape.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.ThemeRepository;
import roomescape.service.dto.ReservationTimeBookedRequest;
import roomescape.service.dto.ReservationTimeBookedResponse;
import roomescape.service.dto.ReservationTimeResponse;
import roomescape.service.dto.ReservationTimeSaveRequest;
import roomescape.exception.RoomEscapeBusinessException;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(
            ReservationTimeRepository reservationTimeRepository,
            ReservationRepository reservationRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationTimeResponse> getTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public ReservationTimeResponse saveTime(ReservationTimeSaveRequest reservationTimeSaveRequest) {
        ReservationTime reservationTime = reservationTimeSaveRequest.toReservationTime();

        validateUniqueReservationTime(reservationTime);

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return new ReservationTimeResponse(savedReservationTime);
    }

    private void validateUniqueReservationTime(ReservationTime reservationTime) {
        boolean isTimeExist = reservationTimeRepository.existByStartAt(reservationTime.getStartAt());

        if (isTimeExist) {
            throw new RoomEscapeBusinessException("중복된 시간입니다.");
        }

    }

    public void deleteTime(Long id) {
        validateDeleteTime(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateDeleteTime(Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new RoomEscapeBusinessException("존재하지 않는 시간입니다.");
        }

        if (reservationRepository.existByTimeId(id)) {
            throw new RoomEscapeBusinessException("예약이 존재하는 시간입니다.");
        }
    }

    public List<ReservationTimeBookedResponse> getTimesWithBooked(
            ReservationTimeBookedRequest reservationTimeBookedRequest) {
        if (themeRepository.findById(reservationTimeBookedRequest.themeId()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 테마입니다.");
        }

        List<ReservationTime> bookedTimes = reservationRepository.findTimeByDateAndThemeId(
                reservationTimeBookedRequest.date(),
                reservationTimeBookedRequest.themeId()
        );
        List<ReservationTime> times = reservationTimeRepository.findAll();

        return times.stream()
                .sorted(Comparator.comparing(ReservationTime::getStartAt))
                .map(time -> ReservationTimeBookedResponse.of(time, bookedTimes.contains(time)))
                .toList();
    }
}
