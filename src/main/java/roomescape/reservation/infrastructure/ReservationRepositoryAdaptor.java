package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.repository.ReservationRepository;

@Repository
public class ReservationRepositoryAdaptor implements ReservationRepository {
    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationJdbcDao reservationJdbcDao;

    public ReservationRepositoryAdaptor(ReservationJpaRepository reservationJpaRepository,
                                        ReservationJdbcDao reservationJdbcDao) {
        this.reservationJpaRepository = reservationJpaRepository;
        this.reservationJdbcDao = reservationJdbcDao;
    }

    @Override
    public Collection<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long id, Long themeId, LocalDate from,
                                                                             LocalDate to) {
        return reservationJpaRepository.findAllByMemberIdAndThemeIdAndDateBetween(id, themeId, from, to);
    }

    @Override
    public boolean existsByDateAndTimeId(LocalDate reservationDate, Long id) {
        return reservationJpaRepository.existsByDateAndTimeId(reservationDate, id);
    }

    @Override
    public Collection<Reservation> findAllByMemberId(Long id) {
        return reservationJdbcDao.findAllByMemberId(id);
    }

    @Override
    public Collection<Reservation> findAll() {
        return reservationJdbcDao.findAll();
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
}
