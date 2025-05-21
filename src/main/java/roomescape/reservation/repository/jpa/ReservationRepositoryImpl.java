package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryImpl(ReservationJpaRepository reservationJpaRepository) {
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public void deleteById(long id) {
        reservationJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return reservationJpaRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public boolean existsByTimeId(long timeId) {
        return reservationJpaRepository.existsByTimeId(timeId);
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return reservationJpaRepository.existsByThemeId(themeId);
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId) {
        return reservationJpaRepository.findAllByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findAllByMemberId(long id) {
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
