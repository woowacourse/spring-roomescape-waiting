package roomescape.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.exception.RoomescapeException;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimesRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimesRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimesRepository = reservationTimesRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationTimeResponse save(ReservationTimeRequest reservationTimeRequest) {
        LocalTime startAt = reservationTimeRequest.startAt();
        if (existsByStartAt(startAt)) {
            throw new RoomescapeException(HttpStatus.CONFLICT,
                    String.format("중복된 예약 시간입니다. 요청 예약 시간:%s", startAt));
        }

        ReservationTime reservationTime = reservationTimesRepository.save(reservationTimeRequest.toReservationTime());
        return ReservationTimeResponse.from(reservationTime);
    }

    private boolean existsByStartAt(LocalTime startAt) {
        return reservationTimesRepository.existsByStartAt(startAt);
    }

    @Transactional
    public void deleteById(Long id) {
        ReservationTime findReservationTime = reservationTimesRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(HttpStatus.NOT_FOUND, "존재하지 않는 예약 시간입니다."));
        Long reservedCount = reservationRepository.countByTimeId(id);
        if (reservedCount > 0) {
            throw new RoomescapeException(HttpStatus.CONFLICT,
                    String.format("해당 예약 시간에 연관된 예약이 존재하여 삭제할 수 없습니다. 삭제 요청한 시간:%s", findReservationTime.getStartAt()));
        }
        reservationTimesRepository.deleteById(id);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimesRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableTimes(LocalDate date, long themeId) {
        return reservationRepository.findAvailableReservationTimes(date, themeId)
                .stream()
                .map(AvailableTimeResponse::from)
                .toList();
    }
}
