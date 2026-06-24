package roomescape.application.query;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.NotFoundException;
import roomescape.presentation.dto.ReservationResponses;

@Service
@Transactional(readOnly = true)
public class ReservationQueryService {

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

    public Optional<Reservation> findBySlot(Slot slot) {
        return reservationRepository.findBySlot(slot);
    }

    public ReservationResponses findMine(Member member) {
        List<Reservation> reservations = reservationRepository.findByMember(member);
        return ReservationResponses.from(reservations, reservations.size(), 0, reservations.size());
    }

    public Set<Long> findReservedTimeIds(LocalDate date, Theme theme) {
        return new HashSet<>(reservationRepository.findReservedTimeIdsByDateAndTheme(date, theme));
    }
}
