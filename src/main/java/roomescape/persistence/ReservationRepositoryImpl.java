package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public ReservationRepositoryImpl(final JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(final Long reservationId) {
        jpaReservationRepository.deleteById(reservationId);
    }

    @Override
    public Optional<Reservation> findById(final Long reservationId) {
        return jpaReservationRepository.findById(reservationId);
    }

    @Override
    public List<Reservation> findByMemberId(final Long memberId) {
        return jpaReservationRepository.findByMemberId(memberId);
    }

    @Override
    public boolean existsByTimeId(final Long reservationTimeId) {
        return jpaReservationRepository.existsByTimeId(reservationTimeId);
    }

    @Override
    public boolean existsDuplicateReservation(final LocalDate reservationDate, final Long timeId, final Long themeId) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeId(reservationDate, timeId, themeId);
    }

    @Override
    public boolean isReservationSlotEmpty(LocalDate reservationDate, Long timeId, Long themeId) {
        return jpaReservationRepository.isReservationSlotEmpty(reservationDate, timeId, themeId);
    }

    @Override
    public boolean existsByThemeId(final Long themeId) {
        return jpaReservationRepository.existsByThemeId(themeId);
    }

    @Override
    public List<Reservation> findReservationsInConditions(final Long memberId, final Long themeId, final LocalDate dateFrom, final LocalDate dateTo) {
        return jpaReservationRepository.findReservationsInConditions(memberId, themeId, dateFrom, dateTo);
    }

    @Override
    public boolean hasAlreadyReserved(Long memberId, Long themeId, Long timeId, LocalDate date) {
        return jpaReservationRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(memberId, themeId, timeId, date);
    }
}
