package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationQueryRepository;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationCommandRepository, ReservationQueryRepository {

    private final JpaReservationRepository jpaReservationRepository;

    @Override
    public Long save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation).getId();
    }

    @Override
    public void deleteById(final Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(
            final LocalDate date,
            final Long timeId,
            final Long themeId
    ) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        return jpaReservationRepository.findById(id);
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(
            final Long themeId,
            final Long memberId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    ) {
        return jpaReservationRepository.findAllByThemeIdAndMemberIdAndDateRange(themeId, memberId, dateFrom, dateTo);
    }

    @Override
    public List<Reservation> findAllByDateAndThemeId(
            final LocalDate date,
            final Long themeId
    ) {
        return jpaReservationRepository.findAllByDateAndThemeId(date, themeId);
    }

    @Override
    public List<Reservation> findAllByMemberId(final Long memberId) {
        return jpaReservationRepository.findAllByMemberId(memberId);
    }
}
