package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;

@Repository
public class JpaReservationRepository implements ReservationRepository {

    private final ReservationListCrudRepository reservationListCrudRepository;

    public JpaReservationRepository(ReservationListCrudRepository reservationRepository) {
        this.reservationListCrudRepository = reservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationListCrudRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationListCrudRepository.findAll();
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservationListCrudRepository.findAllByMemberId(memberId);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return reservationListCrudRepository.findByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDateRange(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        Specification<Reservation> specification = ReservationSpecification.getReservationSpecification(themeId, memberId, from, to);
        return reservationListCrudRepository.findAll(specification);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationListCrudRepository.findById(id);
    }

    @Override
    public Optional<Reservation> findByTimeId(Long id) {
        return reservationListCrudRepository.findByTimeId(id);
    }

    @Override
    public Optional<Reservation> findByThemeId(Long id) {
        return reservationListCrudRepository.findByThemeId(id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservationListCrudRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public void deleteById(Long id) {
        reservationListCrudRepository.deleteById(id);
    }
}
