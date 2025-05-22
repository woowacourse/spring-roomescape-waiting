package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

@Repository
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public JpaReservationRepositoryAdapter(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(final Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return jpaReservationRepository.findById(id);
    }

    @Override
    public List<Reservation> findBy(final LocalDate date, final Long themeId) {
        return jpaReservationRepository.findByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findBy(final Long memberId, final Long themeId, final LocalDate from, final LocalDate to) {
        return jpaReservationRepository.findByMemberIdAndThemeIdAndDateBetween(memberId, themeId, from, to);
    }

    @Override
    public List<Reservation> findBy(Long memberId) {
        return jpaReservationRepository.findByMemberId(memberId);
    }

    @Override
    public List<Reservation> findBy(final Long themeId, final LocalDate date) {
        return jpaReservationRepository.findByThemeIdAndDate(themeId, date);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public boolean existsByTimeId(final Long reservationTimeId) {
        return jpaReservationRepository.existsByTimeId(reservationTimeId);
    }

    @Override
    public boolean existByThemeId(final Long themeId) {
        return jpaReservationRepository.existsByThemeId(themeId);
    }

    @Override
    public Optional<Reservation> findBy(LocalDate date, Long timeId, Long themeId) {
        return jpaReservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }
}
