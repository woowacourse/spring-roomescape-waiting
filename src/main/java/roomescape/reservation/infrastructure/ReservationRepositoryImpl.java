package roomescape.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

import java.time.LocalDateTime;
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
    public Optional<Reservation> findById(final ReservationId id) {
        return jpaReservationRepository.findById(id.getValue());
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
    public List<Reservation> findAllBySlotAndCreatedAt(final ReservationSlot slot, final LocalDateTime createdAt) {
        return jpaReservationRepository.findAllBySlotAndCreatedAtJpql(slot, createdAt);
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
