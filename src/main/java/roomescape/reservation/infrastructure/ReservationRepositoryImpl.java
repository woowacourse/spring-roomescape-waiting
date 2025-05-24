package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    @Override
    public Reservation save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(final Long id) {
        jpaReservationRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaReservationRepository.existsById(id);
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
    @Transactional(readOnly = true)
    public Optional<Reservation> findById(final Long id) {
        return jpaReservationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(
            final Long themeId,
            final Long memberId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    ) {
        return jpaReservationRepository.findAllByThemeIdAndMemberIdAndDateRange(themeId, memberId, dateFrom, dateTo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAllByDateAndThemeId(
            final LocalDate date,
            final Long themeId
    ) {
        return jpaReservationRepository.findAllByDateAndThemeId(date, themeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAllByMemberId(final Long memberId) {
        return jpaReservationRepository.findAllByMemberId(memberId);
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndMemberId(
            final LocalDate date,
            final Long timeId,
            final Long themeId,
            final Long memberId) {
        return jpaReservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                date, timeId, themeId, memberId
        );
    }
}
