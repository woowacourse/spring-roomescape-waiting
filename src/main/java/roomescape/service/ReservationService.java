package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.exception.custom.CannotDeleteReservationTimeInUseException;
import roomescape.exception.custom.CannotDeleteThemeInUseException;
import roomescape.exception.custom.ReservationNotExistsException;
import roomescape.repository.ReservationRepository;
import roomescape.validator.ReservationValidator;
import roomescape.validator.ReservationValidatorFactory;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public Reservation save(Reservation reservationWithoutId, boolean isAdmin) {
        ReservationValidator reservationValidator = ReservationValidatorFactory.getValidator(isAdmin);
        reservationValidator.validateCreate(reservationWithoutId, LocalDateTime.now(clock));
        return reservationRepository.save(reservationWithoutId);
    }

    public List<Reservation> findByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }

    public List<Reservation> findByName(String name) {
        return reservationRepository.findByMember_Name(name);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotExistsException::new);
    }

    @Transactional
    public void delete(Reservation reservation, boolean isAdmin) {
        ReservationValidator reservationValidator = ReservationValidatorFactory.getValidator(isAdmin);
        reservationValidator.validateDelete(reservation, LocalDateTime.now(clock));
        reservationRepository.deleteById(reservation.getId());
    }

    public Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findBySlot(date, timeId, themeId);
    }

    public void validateReferencedTheme(Long themeId) {
        if (reservationRepository.existsBySlot_Theme_Id(themeId)) {
            throw new CannotDeleteThemeInUseException();
        }
    }

    public void validateReferencedTime(Long timeId) {
        if (reservationRepository.existsBySlot_Time_Id(timeId)) {
            throw new CannotDeleteReservationTimeInUseException();
        }
    }
}
