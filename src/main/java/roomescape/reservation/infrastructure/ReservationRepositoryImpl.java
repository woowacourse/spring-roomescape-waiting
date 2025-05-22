package roomescape.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.infrastructure.vo.ThemeBookingCount;
import roomescape.time.domain.ReservationTime;
import roomescape.user.domain.UserId;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    @Override
    public boolean existsById(final Long id) {
        return jpaReservationRepository.existsById(id);
    }

    @Override
    public boolean existsByParams(final Long timeId) {
        return jpaReservationRepository.existsByTimeId(timeId);
    }

    @Override
    public boolean existsByParams(final ReservationDate date, final Long timeId, final Long themeId) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return jpaReservationRepository.findById(id);
    }

    @Override
    public List<Long> findTimeIdByParams(final ReservationDate date, final Long themeId) {
        return jpaReservationRepository.findAllByDateAndThemeId(date, themeId).stream()
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
    public void deleteById(final Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public List<ThemeBookingCount> findThemesToBookedCount(final ReservationDate startDate, final ReservationDate endDate, final int count) {
        Pageable topN = PageRequest.of(0, count);
        return jpaReservationRepository.findThemesWithBookedCount(startDate, endDate, topN);
    }

    @Override
    public List<Reservation> findAllByParams(final UserId userId, final Long themeId, final ReservationDate from, final ReservationDate to) {
        Specification<Reservation> spec = Specification.where(ReservationSpecs.isMemberReservation(userId))
                .and(ReservationSpecs.isThemeReservation(themeId))
                .and(ReservationSpecs.isReservationByPeriod(from, to));
        return jpaReservationRepository.findAll(spec);
    }
}
