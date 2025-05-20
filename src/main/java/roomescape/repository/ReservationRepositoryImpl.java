package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.repository.jpa.ReservationJpaRepository;
import roomescape.repository.jpa.ReservationSpecification;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryImpl(final ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        return reservationJpaRepository.findById(id);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public void  deleteById(final long id) {
        reservationJpaRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(final long memberId, final long themeId,
                                                                          final LocalDate dateFrom,
                                                                          final LocalDate dateTo) {
        Specification<Reservation> spec = Specification.where(ReservationSpecification.hasThemeId(themeId))
                .and(ReservationSpecification.betweenDateFromAndDateTo(dateFrom, dateTo))
                .and(ReservationSpecification.hasThemeId(memberId));
        return reservationJpaRepository.findAll(spec);
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservationJpaRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public List<Reservation> findByMemberId(final long memberId) {
        return reservationJpaRepository.findByMemberId(memberId);
    }

    @Override
    public boolean existById(final long id) {
        return reservationJpaRepository.existsById(id);
    }

    @Override
    public boolean existByThemeId(final long themeId) {
        return reservationJpaRepository.existsByThemeId(themeId);
    }

    @Override
    public boolean existByTimeId(final long timeId) {
        return reservationJpaRepository.existsByTimeId(timeId);
    }
}
