package roomescape.persistence;

import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.service.result.WaitingWithRank;

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
        return jpaReservationRepository.findByMemberIdWithDetails(memberId);
    }

    @Override
    public List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId) {
        return jpaReservationRepository.findWaitingsWithRankByMemberId(memberId);
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
    public boolean existsAlreadyReserved(LocalDate reservationDate, Long timeId, Long themeId) {
        return jpaReservationRepository.existsAlreadyReserved(reservationDate, timeId, themeId);
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
    public List<Reservation> findWaitingsReservation() {
        return jpaReservationRepository.findWaitingReservations();
    }

    @Override
    public Optional<Reservation> findFirstWaiting(LocalDate date, Long themeId, Long timeId) {
        return jpaReservationRepository.findFirstWaiting(date, themeId, timeId);
    }

    @Override
    public boolean isFirstWaiting(Long reservationId, LocalDate date, Long themeId, Long timeId) {
        return jpaReservationRepository.isFirstWaiting(reservationId, date, themeId, timeId);
    }
}
