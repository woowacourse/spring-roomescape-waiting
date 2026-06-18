package roomescape.reservation.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.application.port.out.ReservationRepository;
import roomescape.reservation.application.port.out.projection.ReservationDetailProjection;
import roomescape.reservation.domain.Reservation;

@Repository
@RequiredArgsConstructor
public class JpaReservationRepository implements ReservationRepository {
    private final SpringDataReservationRepository repository;

    @Override
    public Reservation save(Reservation reservation) {
        return repository.save(reservation);
    }

    @Override
    public List<ReservationDetailProjection> findAll() {
        return repository.findAllDetails();
    }

    @Override
    public Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId) {
        return Set.copyOf(repository.findTimeIdsByDateAndThemeId(date, themeId));
    }

    @Override
    public List<ReservationDetailProjection> findAllReservationDetailsByMemberId(long memberId) {
        return repository.findAllReservationDetailsByMemberId(memberId);
    }

    @Override
    public void deleteById(long reservationId) {
        repository.deleteById(reservationId);
        repository.flush();
    }

    @Override
    public Optional<Reservation> findById(long reservationId) {
        return repository.findById(reservationId);
    }

    @Override
    public boolean existsBySlotId(long slotId) {
        return repository.existsBySlot_Id(slotId);
    }

    @Override
    public boolean existsByMemberIdAndSlotId(long memberId, long slotId) {
        return repository.existsByMember_IdAndSlot_Id(memberId, slotId);
    }
}
