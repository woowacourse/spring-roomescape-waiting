package roomescape.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.ReservationTimeRepository;
import roomescape.domain.reservation.slot.Theme;
import roomescape.domain.reservation.slot.ThemeRepository;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.ReservationTimeBookedRequest;
import roomescape.service.dto.ReservationTimeBookedResponse;
import roomescape.service.dto.ReservationTimeResponse;
import roomescape.service.dto.ReservationTimeSaveRequest;

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

    @Transactional
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

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> getTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeBookedResponse> getTimesWithBooked(
            ReservationTimeBookedRequest reservationTimeBookedRequest) {
        Theme foundTheme = themeRepository.findById(reservationTimeBookedRequest.themeId())
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));

        List<ReservationTime> bookedTimes = reservationRepository.findTimesByDateAndTheme(
                reservationTimeBookedRequest.date(),
                foundTheme
        );
        List<ReservationTime> times = reservationTimeRepository.findAll();

        return times.stream()
                .sorted(Comparator.comparing(ReservationTime::getStartAt))
                .map(time -> ReservationTimeBookedResponse.of(time, bookedTimes.contains(time)))
                .toList();
    }

    @Transactional
    public void deleteTime(Long id) {
        ReservationTime foundTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 시간입니다."));

        if (reservationRepository.existsBySlot_Time(foundTime)) {
            throw new RoomEscapeBusinessException("예약이 존재하는 시간입니다.");
        }

        reservationTimeRepository.delete(foundTime);
    }
}
