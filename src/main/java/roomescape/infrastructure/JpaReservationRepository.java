package roomescape.infrastructure;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.vo.Id;
import roomescape.presentation.dto.response.ReservationWithAhead;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JpaReservationRepository implements ReservationRepository {

    private final JpaReservationDao dao;

    public JpaReservationRepository(JpaReservationDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(Reservation reservation) {
        dao.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return dao.findAll();
    }

    @Override
    public List<Reservation> findAllWithFilter(Id themeId, Id memberId, LocalDate dateFrom, LocalDate dateTo) {
        return dao.findAllWithFilter(themeId, memberId, dateFrom, dateTo);
    }

    @Override
    public List<ReservationWithAhead> findReservationsWithAhead(Id userId) {
        return dao.findReservationsWithAhead(userId);
    }

    @Override
    public Optional<Reservation> findById(Id id) {
        return dao.findById(id);
    }

    @Override
    public boolean existById(Id reservationId) {
        return dao.existsById(reservationId);
    }

    @Override
    public boolean existByTimeId(Id timeId) {
        return dao.existsByTimeId(timeId);
    }

    @Override
    public boolean existByThemeId(Id themeId) {
        return dao.existsByThemeId(themeId);
    }

    @Override
    public boolean isDuplicateDateAndTimeAndTheme(LocalDate date, LocalTime time, Id themeId) {
        return dao.existsByDateValueAndTimeStartTimeValueAndThemeId(date, time, themeId);
    }

    @Override
    public void deleteById(Id reservationId) {
        dao.deleteById(reservationId);
    }
}
