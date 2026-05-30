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
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final String RESERVATION_NOT_FOUND_FORMAT = "ID %d번 예약을 찾을 수 없습니다.";
    private static final String ALREADY_EXISTS_RESERVATION = "해당 날짜와 시간, 테마에 이미 예약이 존재합니다.";
    private static final String PAST_RESERVATION_CANCEL_REJECTED = "이미 지난 예약은 취소할 수 없습니다.";
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

    public boolean hasReservationsByTimeId(Long timeId) {
        return reservationRepository.existsByTimeId(timeId);
    }

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

    // TODO: Validate 로직 파편화
    //  isOwnedBy가 문제인듯. 바꿀때는 도메인에서 검사하도록하면 됨.
    @Transactional
    public void cancelMyReservation(Long id, String name) {
        Reservation reservation = findMyReservation(id, name);

        if (reservation.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_CANCEL_REJECTED);
        }

        reservationRepository.deleteById(id);
    }

    public boolean hasReservationsByThemeId(Long themeId) {
        return reservationRepository.existsByThemeId(themeId);
    }

    public void validateConflict(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsBySlot(date, timeId, themeId)) {
            throw new ConflictException(ALREADY_EXISTS_RESERVATION);
        }
    }

    public Set<Long> getReservedTimeIds(LocalDate date, Long themeId) {
        return new HashSet<>(reservationRepository.findReservedTimeIdsByDateAndThemeId(date, themeId));
    }

    public Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_FOUND_FORMAT.formatted(id)));
    }
}
