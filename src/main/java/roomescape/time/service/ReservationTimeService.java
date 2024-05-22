package roomescape.time.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.IllegalRequestException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.dto.AvailableTimeResponse;
import roomescape.time.dto.ReservationTimeAddRequest;
import roomescape.time.dto.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTimeResponse> findAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public List<AvailableTimeResponse> findAllWithReservationStatus(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<ReservationTime> reservedTime = reservationTimeRepository.findReservedTime(date, themeId);

        return reservationTimes.stream()
                .map(time -> new AvailableTimeResponse(time, reservedTime.contains(time)))
                .toList();
    }

    public ReservationTimeResponse saveReservationTime(ReservationTimeAddRequest reservationTimeAddRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeAddRequest.startAt())) {
            throw new IllegalRequestException("이미 존재하는 예약시간은 추가할 수 없습니다.");
        }
        ReservationTime saved = reservationTimeRepository.save(reservationTimeAddRequest.toReservationTime());
        return new ReservationTimeResponse(saved);
    }

    public void removeReservationTime(Long id) {
        reservationTimeRepository.deleteById(id);
    }
}
