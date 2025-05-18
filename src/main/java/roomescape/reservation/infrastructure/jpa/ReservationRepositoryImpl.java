package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationRepository;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryImpl(final ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId) {
        return reservationJpaRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public boolean existsByTimeId(final long timeId) {
        return reservationJpaRepository.existsByTimeId(timeId);
    }

    @Override
    public boolean existsByThemeId(final long themeId) {
        return reservationJpaRepository.existsByThemeId(themeId);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public void deleteById(final long id) {
        reservationJpaRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(final LocalDate date, final long themeId) {
        return reservationJpaRepository.findAllByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findAllByMemberId(final long id) {
        return reservationJpaRepository.findAllByMemberId(id);
    }

    @Override
    public List<Reservation> findAllByCondition(final Long memberId, final Long themeId, final LocalDate from,
                                                final LocalDate to) {
        Specification<Reservation> spec = Specification.where(ReservationSpecs.isMemberReservation(memberId))
                .and(ReservationSpecs.isThemeReservation(themeId))
                .and(ReservationSpecs.isReservationByPeriod(from, to));
        return reservationJpaRepository.findAll(spec);
    }
}
