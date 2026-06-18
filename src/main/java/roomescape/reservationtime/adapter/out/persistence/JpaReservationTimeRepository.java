package roomescape.reservationtime.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.application.port.out.ReservationTimeRepository;
import roomescape.reservationtime.domain.ReservationTime;

@Repository
@RequiredArgsConstructor
public class JpaReservationTimeRepository implements ReservationTimeRepository {
    private final SpringDataReservationTimeRepository repository;

    @Override
    public ReservationTime save(ReservationTime time) {
        return repository.save(time);
    }

    @Override
    public List<ReservationTime> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<ReservationTime> findTimesByDateAndThemeId(LocalDate date, long themeId) {
        return repository.findTimesByDateAndThemeId(date, themeId);
    }

    @Override
    public boolean existsAlreadyTime(LocalTime startAt) {
        return repository.existsByStartAt(startAt);
    }
}
