package roomescape.application.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.api.dto.ReservationResponses;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.NotFoundException;
import roomescape.repository.ReservationRepository;

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
        Page<Reservation> reservations = reservationRepository.findAll(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Order.desc("slot.date"),
                                Sort.Order.asc("slot.time.startAt")
                        )
                )
        );
        return ReservationResponses.from(
                reservations.getContent(),
                reservations.getTotalElements(),
                page,
                size
        );
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 예약입니다. Id: " + id));
    }

    public Optional<Reservation> findBySlot(Slot slot) {
        return reservationRepository.findBySlot(slot);
    }

    public ReservationResponses findMine(Member member) {
        List<Reservation> reservations = reservationRepository.findByReserver(member);
        return ReservationResponses.from(reservations, reservations.size(), 0, reservations.size());
    }

    public Set<Long> findReservedTimeIds(LocalDate date, Theme theme) {
        return reservationRepository.findBySlot_DateAndSlot_Theme(date, theme)
                .stream()
                .map(reservation -> reservation.getTime().getId())
                .collect(Collectors.toSet());
    }
}
