package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.infrastructure.db.ReservationJpaRepository;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public boolean isDuplicatedForDateAndReservationTime(LocalDate date, ReservationTime time) {
        return reservationJpaRepository.findByDateAndReservationTime(date, time).isPresent();
    }

    @Override
    public List<Reservation> findForThemeAndMemberInPeriod(Long themeId, Long memberId, LocalDate startDate,
                                                           LocalDate endDate) {
        return reservationJpaRepository.findByThemeIdAndMemberIdAndDateBetween(
                themeId,
                memberId,
                startDate,
                endDate
        );
    }

    @Override
    public List<Reservation> findForThemeOnDate(Long themeId, LocalDate date) {
        return reservationJpaRepository.findByThemeIdAndDate(themeId, date);
    }

    @Override
    public List<Reservation> findForMember(Long memberId) {
        return reservationJpaRepository.findByMemberId(memberId);
    }

    @Override
    public List<Reservation> findForTheme(Long themeId) {
        return reservationJpaRepository.findByThemeId(themeId);
    }

    @Override
    public List<Reservation> findForReservationTime(Long reservationTimeId) {
        return reservationJpaRepository.findByReservationTimeId(reservationTimeId);
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        reservationJpaRepository.deleteById(id);
    }
}
