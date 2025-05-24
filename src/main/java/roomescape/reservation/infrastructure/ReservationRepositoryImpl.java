package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;

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

    public boolean existsByReservationSlot(
            final ReservationSlot reservationSlot
    ) {
        return jpaReservationRepository.existsByReservationSlot(reservationSlot);
    }

    @Override
    public boolean existsByReservationSlotAndMember(ReservationSlot reservationSlot, Member member) {
        return jpaReservationRepository.existsByReservationSlotAndMember(reservationSlot, member);
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
    public List<Reservation> findAllByDateAndTheme(
            final LocalDate date,
            final Theme theme
    ) {
        return jpaReservationRepository.findAllByDateAndTheme(date, theme);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByMember(final Member member) {
        return jpaReservationRepository.findAllByMember(member);
    }
}
