package roomescape.persistence;

import java.time.LocalDate;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;

import java.util.List;
import java.util.Optional;
import roomescape.persistence.dto.ReservationTimeAvailabilityData;

@Repository
public class ReservationTimeRepositoryImpl implements ReservationTimeRepository {

    private final JpaReservationTimeRepository jpaReservationTimeRepository;

    public ReservationTimeRepositoryImpl(final JpaReservationTimeRepository jpaReservationTimeRepository) {
        this.jpaReservationTimeRepository = jpaReservationTimeRepository;
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        return jpaReservationTimeRepository.save(reservationTime);
    }

    @Override
    public Optional<ReservationTime> findById(final Long reservationTimeId) {
        return jpaReservationTimeRepository.findById(reservationTimeId);
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaReservationTimeRepository.findAll();
    }

    @Override
    public void deleteById(final Long reservationTimeId) {
        jpaReservationTimeRepository.deleteById(reservationTimeId);
    }

    @Override
    public List<ReservationTimeAvailabilityData> findAvailableTimesByThemeAndDate(Long themeId,
                                                                                  LocalDate reservationDate) {
        return jpaReservationTimeRepository.findAvailableTimesByThemeAndDate(themeId, reservationDate);
    }
}
