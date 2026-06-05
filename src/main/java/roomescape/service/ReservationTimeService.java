package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.dto.reservationtime.AvailableReservationTimeResponse;
import roomescape.exception.ReferencedDataException;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTimeResponse> readAll() {
        return reservationTimeRepository.findAllReservationTime().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> readAvailable(LocalDate date, Long themeId) {
        return reservationTimeRepository.findAvailableReservationTime(date, themeId).stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse create(ReservationTimeRequest reservationTimeReq) {
        Long generatedId = reservationTimeRepository.insert(new ReservationTime(null, reservationTimeReq.startAt()));
        return ReservationTimeResponse.from(new ReservationTime(generatedId, reservationTimeReq.startAt()));
    }

    public void update(ReservationTimeRequest newReservationTimeReq, Long id) {
        reservationTimeRepository.save(id, newReservationTimeReq.startAt());
    }

    public void delete(Long id) {
        try {
            reservationTimeRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ReferencedDataException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }
}
