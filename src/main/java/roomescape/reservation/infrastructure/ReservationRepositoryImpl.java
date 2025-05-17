package roomescape.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.domain.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JdbcTemplateReservationRepository jdbcTemplateReservationRepository;
    private final JpaReservationRepository jpaReservationRepository;

    @Override
    public boolean existsByParams(final ReservationId id) {
        return jpaReservationRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsByParams(final ReservationTimeId timeId) {
        return jpaReservationRepository.existsByTimeId(timeId.getValue());
    }

    @Override
    public boolean existsByParams(final ReservationDate date, final ReservationTimeId timeId, final ThemeId themeId) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId.getValue(), themeId.getValue());
    }

    @Override
    public Optional<Reservation> findById(final ReservationId id) {
        return jpaReservationRepository.findById(id.getValue());
    }

    @Override
    public List<ReservationTimeId> findTimeIdByParams(final ReservationDate date, final ThemeId themeId) {
        return jpaReservationRepository.findAllByDateAndThemeId(date, themeId.getValue()).stream()
                .map(Reservation::getTime)
                .map(ReservationTime::getId)
                .toList();
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public List<Reservation> findAllByUserId(final UserId userId) {
        return jpaReservationRepository.findAllByUserId(userId);
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(final ReservationId id) {
        jpaReservationRepository.deleteById(id.getValue());
    }

    @Override
    public Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(final ReservationDate startDate, final ReservationDate endDate, final int count) {
        return jdbcTemplateReservationRepository.findThemesToBookedCountByParamsOrderByBookedCount(startDate, endDate, count);
    }

    @Override
    public List<Reservation> findAllByParams(final UserId userId, final ThemeId themeId, final ReservationDate from, final ReservationDate to) {
        return jdbcTemplateReservationRepository.findAllByParams(userId, themeId, from, to);
    }
}
