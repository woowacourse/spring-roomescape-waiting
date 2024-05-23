package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.TimeSaveRequest;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.dto.response.TimeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Transactional
    public TimeResponse save(TimeSaveRequest timeSaveRequest) {
        ReservationTime reservationTime = timeSaveRequest.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return TimeResponse.toResponse(savedReservationTime);
    }

    public TimeResponse findById(Long id) {
        ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다"));

        return TimeResponse.toResponse(reservationTime);
    }

    public List<AvailableReservationTimeResponse> findAvailableTimes(LocalDate date, Long themeId) {
        List<Long> bookedTimeIds = reservationRepository.findTimeIdsByDateAndThemeId(date, themeId);

        return reservationTimeRepository.findAll()
                .stream()
                .map(reservationTime -> createResponse(reservationTime, bookedTimeIds))
                .toList();
    }

    private static AvailableReservationTimeResponse createResponse(
            ReservationTime reservationTime,
            List<Long> bookedTimeIds
    ) {
        return AvailableReservationTimeResponse.toResponse(reservationTime, isBooked(reservationTime, bookedTimeIds));
    }

    private static boolean isBooked(ReservationTime reservationTime, List<Long> bookedTimeIds) {
        return bookedTimeIds.contains(reservationTime.getId());
    }

    public List<TimeResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(TimeResponse::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findReservationTimesThatReservationReferById(id);
        if (!reservationTimes.isEmpty()) {
            throw new IllegalArgumentException("해당 시간으로 예약된 내역이 있습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
