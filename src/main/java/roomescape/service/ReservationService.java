package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationResponses;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final String RESERVATION_NOT_FOUND_FORMAT = "ID %d번 예약을 찾을 수 없습니다.";
    private static final String NOT_OWNER = "본인의 예약이 아닙니다.";

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponses getReservationPage(int page, int size) {
        List<Reservation> reservations = reservationRepository.findAll(page * size, size);
        long totalCount = reservationRepository.count();
        return ReservationResponses.from(reservations, totalCount, page, size);
    }

    public ReservationResponses getMyReservations(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        return ReservationResponses.from(reservations, reservations.size(), 0, reservations.size());
    }
    
    // TODO: 테스트에서만 사용됨
    public Reservation findMyReservation(Long id, String name) {
        Reservation reservation = getById(id);
        if (!reservation.isOwnedBy(name)) {
            throw new UnauthorizedException(NOT_OWNER);
        }
        return reservation;
    }

    @Transactional
    public Reservation addReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateReservation(Reservation reservation) {
        return reservationRepository.update(reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void cancelMyReservation(Long id, String name) {
        Reservation reservation = getById(id);
        reservation.cancelBy(name, LocalDateTime.now());

        reservationRepository.deleteById(id);
    }

    public boolean hasReservationsByThemeId(Long themeId) {
        return reservationRepository.existsByThemeId(themeId);
    }

    public Set<Long> getReservedTimeIds(LocalDate date, Long themeId) {
        return new HashSet<>(reservationRepository.findReservedTimeIdsByDateAndThemeId(date, themeId));
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_FOUND_FORMAT.formatted(id)));
    }
}
