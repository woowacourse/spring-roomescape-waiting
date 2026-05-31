package roomescape.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ReservationResponses;
import roomescape.domain.Reservation;
import roomescape.domain.exception.ConflictException;
import roomescape.domain.exception.NotFoundException;
import roomescape.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

    private static final String WAITING_REQUIRES_RESERVED_SLOT = "예약된 슬롯에만 대기를 신청할 수 있습니다.";

    private final ReservationRepository reservationRepository;

    public ReservationQueryService(
            ReservationRepository reservationRepository
    ) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponses findPage(int page, int size) {
        List<Reservation> reservations = reservationRepository.findAll(page * size, size);
        long totalCount = reservationRepository.count();
        return ReservationResponses.from(reservations, totalCount, page, size);
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 예약입니다. Id: " + id));
    }

    public Reservation findBySlot(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findBySlot(date, timeId, themeId)
                .orElseThrow(() -> new ConflictException(WAITING_REQUIRES_RESERVED_SLOT));
    }

    public ReservationResponses findMine(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        return ReservationResponses.from(reservations, reservations.size(), 0, reservations.size());
    }

    public Set<Long> findReservedTimeIds(LocalDate date, Long themeId) {
        return new HashSet<>(reservationRepository.findReservedTimeIdsByDateAndThemeId(date, themeId));
    }
}
