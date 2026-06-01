package roomescape.application.command;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;

@Service
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationCommandService(
            ReservationRepository reservationRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    @Transactional
    public Reservation save(String name, LocalDate date, ReservationTime reservationTime, Theme theme) {
        Reservation reservation = Reservation.createWith(
                name,
                date,
                reservationTime,
                theme,
                now()
        );

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateMine(Reservation existing, String name, LocalDate date, ReservationTime newTime) {
        Reservation updated = existing.updateWith(
                name,
                date,
                newTime,
                now()
        );

        return reservationRepository.update(updated);
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteMine(Reservation reservation, String name) {
        reservation.cancelBy(name, now());

        reservationRepository.deleteById(reservation.getId());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
