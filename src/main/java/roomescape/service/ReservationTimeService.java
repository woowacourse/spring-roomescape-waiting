package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime save(ServiceReservationTimeCreateRequest request) {
        validateDuplicatedReservationTime(request.startAt());
        return reservationTimeRepository.save(request.toEntity());
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public List<Long> findReservedTimeIdsByDateAndTheme(LocalDate date, Long themeId) {
        return reservationTimeRepository.findReservedTimeIdByDateAndTheme(date, themeId);
    }

    public void delete(Long id) {
        reservationTimeRepository.delete(id);
    }

    public ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomEscapeException(DomainErrorCode.NOT_FOUND_RESERVATION_TIME));
    }

    private void validateDuplicatedReservationTime(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new RoomEscapeException(DomainErrorCode.DUPLICATED_RESERVATION_TIME);
        }
    }
}
