package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.repository.ReservationRepository;
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

    public boolean existsByReservationSlot(
            final ReservationSlot reservationSlot
    ) {
        return jpaReservationRepository.existsByReservationSlot(reservationSlot);
    }

    @Override
    public Reservation getById(final Long reservationId) {
        return jpaReservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약을 찾을 수 없습니다. id = " + reservationId));
    }

    @Override
    public Optional<Reservation> findByReservationSlot(final ReservationSlot reservationSlot) {
        return jpaReservationRepository.findByReservationSlot(reservationSlot);
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
    public List<Reservation> findAllByDateAndTheme(
            final LocalDate date,
            final Theme theme
    ) {
        return jpaReservationRepository.findAllByDateAndTheme(date, theme);
    }

    @Override
    public List<Reservation> findAllByMember(final Member member) {
        return jpaReservationRepository.findAllByMember(member);
    }
}
