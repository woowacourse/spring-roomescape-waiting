package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Reservations;
import roomescape.dto.ReservationResponses;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

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

    public ReservationResponses getMyReservations(String name, int page, int size) {
        List<Reservation> reservations = reservationRepository.findByName(name, page * size, size);
        long totalCount = reservationRepository.countByName(name);
        return ReservationResponses.from(reservations, totalCount, page, size);
    }

    public boolean hasReservationsByTimeId(Long timeId) {
        return reservationRepository.existsByTimeId(timeId);
    }

    public Optional<Reservation> findReservation(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation findMyReservation(Long id, String name) {
        Reservation reservation = findReservation(id)
                .orElseThrow(() -> NotFoundException.reservation(id));
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
    public void transferWithPendingStatus(Long id, String name) {
        reservationRepository.transferWithPendingStatus(id, name);
    }

    @Transactional
    public void confirm(Long id) {
        reservationRepository.confirm(id);
    }

    public boolean hasReservationsByThemeId(Long themeId) {
        return reservationRepository.existsByThemeId(themeId);
    }

    public Reservations findByDateAndThemeId(LocalDate date, Long themeId) {
        return reservationRepository.findByDateAndThemeId(date, themeId);
    }
}
