package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Repository;
import roomescape.infrastructure.db.ReservationJpaRepository;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;
    private final JpaContext jpaContext;

    @Override
    public Optional<Reservation> findDuplicatedReservationByDateAndTime(LocalDate date, ReservationTime time) {
        return reservationJpaRepository.findByDateAndReservationTime(date, time);
    }

    @Override
    public List<Reservation> findReservationsForThemeAndMemberInPeriod(Long themeId, Long memberId, LocalDate startDate,
                                                                       LocalDate endDate) {
        return reservationJpaRepository.findByThemeIdAndMemberIdAndDateBetween(
                themeId,
                memberId,
                startDate,
                endDate
        );
    }

    @Override
    public List<Reservation> findReservationsForThemeOnDate(Long themeId, LocalDate date) {
        return reservationJpaRepository.findByThemeIdAndDate(themeId, date);
    }

    @Override
    public List<Reservation> findReservationForMember(Long memberId) {
        return reservationJpaRepository.findByMemberId(memberId);
    }

    @Override
    public List<Reservation> findReservationForTheme(Long themeId) {
        return reservationJpaRepository.findByThemeId(themeId);
    }

    @Override
    public List<Reservation> findReservationForReservationTime(Long reservationTimeId) {
        return reservationJpaRepository.findByReservationTimeId(reservationTimeId);
    }

    @Override
    public Reservation saveReservation(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAllReservations() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public void deleteWithId(Long id) {
        reservationJpaRepository.deleteById(id);
    }
}
