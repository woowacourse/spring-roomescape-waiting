package roomescape.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.infrastructure.projection.TimeValueProjection;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.domain.TimeValue;
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
    public boolean existsByParams(final ReservationDate date, final TimeValue time, final ThemeId themeId) {
        return jpaReservationRepository.existsByDateAndTimeAndThemeId(date, time, themeId.getValue());
    }

    @Override
    public Optional<Reservation> findById(final ReservationId id) {
        return jpaReservationRepository.findById(id.getValue());
    }

    @Override
    public List<TimeValue> findTimeValuesByParams(final ReservationDate date, final ThemeId themeId) {
        return jpaReservationRepository.findTimeByDateAndThemeId(date, themeId.getValue()).stream()
                .map(TimeValueProjection::getTime)
                .toList();
    }

    @Override
    public List<ReservationTimeId> findTimeIdByParams(final ReservationDate date, final ThemeId themeId) {
        return List.of();
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
    public Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(final ReservationDate startDate, final ReservationDate endDate, final int count) {
        return jdbcTemplateReservationRepository.findThemesToBookedCountByParamsOrderByBookedCount(startDate, endDate, count);
    }

    @Override
    public List<Reservation> findAllByParams(final UserId userId, final ThemeId themeId, final ReservationDate from, final ReservationDate to) {
        return jdbcTemplateReservationRepository.findAllByParams(userId, themeId, from, to);
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
    public void delete(final Reservation target) {
        jpaReservationRepository.delete(target);
    }
}
