package roomescape.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.ReservationTimeBookedRequest;
import roomescape.service.dto.response.ReservationTimeBookedResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.dto.request.ReservationTimeSaveRequest;

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
        boolean isTimeExist = reservationTimeRepository.existsByStartAt(reservationTime.getStartAt());

        if (isTimeExist) {
            throw new RoomEscapeBusinessException("중복된 시간입니다.");
        }

    }

    public void deleteTime(Long id) {
        ReservationTime foundTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 시간입니다."));

        if (reservationRepository.existsByTime(foundTime)) {
            throw new RoomEscapeBusinessException("예약이 존재하는 시간입니다.");
        }

        reservationTimeRepository.delete(foundTime);
    }

    public List<ReservationTimeBookedResponse> getTimesWithBooked(
            ReservationTimeBookedRequest reservationTimeBookedRequest) {
        Theme foundTheme = themeRepository.findById(reservationTimeBookedRequest.themeId())
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));

        List<ReservationTime> bookedTimes = makeBookedTimes(reservationTimeBookedRequest.date(), foundTheme);
        List<ReservationTime> times = reservationTimeRepository.findAll();

        return times.stream()
                .sorted(Comparator.comparing(ReservationTime::getStartAt))
                .map(time -> ReservationTimeBookedResponse.of(time, bookedTimes.contains(time)))
                .toList();
    }

    private List<ReservationTime> makeBookedTimes(LocalDate date, Theme foundTheme) {
        return reservationRepository.findByDateAndTheme(date,  foundTheme)
                .stream()
                .map(Reservation::getTime)
                .toList();
    }
}
