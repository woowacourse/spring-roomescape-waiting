package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;

@Repository
@AllArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepository {
    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long id, Long themeId, LocalDate from,
                                                                       LocalDate to) {
        return reservationJpaRepository.findAllByMemberIdAndThemeIdAndDateBetween(id, themeId, from, to);
    }

    @Override
    public boolean existsByDateAndTimeId(LocalDate reservationDate, Long id) {
        return reservationJpaRepository.existsBySpecDateAndSpecTimeId(reservationDate, id);
    }

    @Override
    public List<Reservation> findAllByMemberId(Long id) {
        return reservationJpaRepository.findAllByMemberId(id);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAllWithEager();
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public void deleteById(Long id) {
        reservationJpaRepository.deleteById(id);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public boolean existsBySpec(ReservationSpec spec) {
        return reservationJpaRepository.existsBySpec(spec);
    }
}
