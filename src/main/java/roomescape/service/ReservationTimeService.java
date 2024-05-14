package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation.Reservation;
import roomescape.domain.ReservationTime.ReservationTime;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository,
                                  ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public Long addReservationTime(ReservationTimeRequest reservationTimeRequest) {
        validateTimeDuplicate(reservationTimeRequest.startAt());
        ReservationTime reservationTime = reservationTimeRequest.toEntity();
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTimeResponse> getAllReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse getReservationTime(Long id) {
        ReservationTime reservationTime = findTimeById(id);
        return ReservationTimeResponse.from(reservationTime);
    }

    public List<ReservationTimeResponse> getAvailableTimes(LocalDate date, Long themeId) {
        List<Long> bookedTimeIds = findTimeIdForDateAndTheme(date, themeId);
        List<ReservationTime> availableTimes = reservationTimeRepository.findByIdsNotIn(bookedTimeIds);
        return availableTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public void deleteReservationTime(Long id) {
        ReservationTime reservationTime = findTimeById(id);
        validateDeletable(reservationTime);
        reservationRepository.delete(reservationTime.getId());
    }

    private ReservationTime findTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 잘못된 예약시간 정보 입니다.",
                        new Throwable("time_id : " + timeId)
                ));
    }

    private List<Long> findTimeIdForDateAndTheme(LocalDate date, Long themeId) {
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, themeId);
        return reservations.stream()
                .map(Reservation::getTimeId)
                .toList();
    }

    private void validateTimeDuplicate(LocalTime time) {
        if (reservationTimeRepository.existTime(time)) {
            throw new IllegalArgumentException(
                    "[ERROR] 이미 등록된 시간은 등록할 수 없습니다.",
                    new Throwable("등록 시간 : " + time)
            );
        }
    }

    private void validateDeletable(ReservationTime reservationTime) {
        if (reservationRepository.existTimeId(reservationTime.getId())) {
            throw new IllegalArgumentException(
                    "[ERROR] 해당 시간에 예약이 존재해서 삭제할 수 없습니다.",
                    new Throwable("예약 시간 : " + reservationTime.getStartAt())
            );
        }
    }
}
