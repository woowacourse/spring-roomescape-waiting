package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;

@Repository
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public JpaReservationRepositoryAdapter(final JpaReservationRepository jpaReservationRepository) {
        this.jpaReservationRepository = jpaReservationRepository;
    }

    @Override
    public List<Reservation> findFilteredReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate,
                                                      final LocalDate endDate) {
        return jpaReservationRepository.findFilteredReservations(themeId, memberId, startDate, endDate);
    }

    @Override
    public boolean existsByTimeId(final Long id) {
        return jpaReservationRepository.existsByTimeId(id);
    }

    @Override
    public boolean existsByThemeId(final Long id) {
        return jpaReservationRepository.existsByThemeId(id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public List<AvailableReservationTimeResponse> findBookedTimesByDateAndThemeId(final LocalDate date,
                                                                                  final Long themeId) {
        return jpaReservationRepository.findBookedTimesByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findByMemberId(final Long id) {
        return jpaReservationRepository.findAllByMemberId(id);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public void deleteById(final Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }
}
