package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationWithRank;

@Repository
public class ReservationJpaRepository implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public ReservationJpaRepository(JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return jpaReservationRepository.findByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findAllWaitingReservations(LocalDateTime now) {
        return jpaReservationRepository.findAllWaitingReservations(now);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDate(Long memberId, Long themeId, LocalDate dateFrom,
                                                             LocalDate dateTo) {
        return jpaReservationRepository.findByMemberIdAndThemeIdAndDate(memberId, themeId, dateFrom, dateTo);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        return jpaReservationRepository.existsByTimeId(timeId);
    }

    @Override
    public boolean hasReservedReservation(Reservation reservation) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeIdAndStatus(reservation.getDate(),
                reservation.timeId(), reservation.themeId(), ReservationStatus.RESERVED);
    }

    @Override
    public boolean hasSameReservation(Reservation reservation) {
        return jpaReservationRepository.existsReservation(reservation.getDate(), reservation.timeId(),
                reservation.themeId(), reservation.memberId(), reservation.getStatus());
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return jpaReservationRepository.existsByThemeId(themeId);
    }

    @Override
    public List<ReservationWithRank> findReservationWithRankByMemberId(Long memberId) {
        return jpaReservationRepository.findReservationWithRankById(memberId);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return jpaReservationRepository.findById(id);
    }

    @Override
    public void changeReservationStatus(Long id, ReservationStatus status) {
        jpaReservationRepository.changeReservationStatus(id, status);
    }
}
