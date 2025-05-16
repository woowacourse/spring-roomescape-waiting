package roomescape.repository.fake;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> data = new HashMap<>();
    private Long autoIncrementId = 1L;

    @Override
    public Reservation save(Reservation reservation) {
        Reservation reservationToSave = Reservation.generateWithPrimaryKey(reservation, autoIncrementId);
        data.put(autoIncrementId, reservationToSave);
        autoIncrementId++;

        return reservationToSave;
    }

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(data.values());
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        data.remove(id);
    }

    @Override
    public boolean existsByTimeId(Long id) {
        if (id == null) {
            return false;
        }
        return data.values().stream()
            .anyMatch(reservation -> reservation.getTime().getId().equals(id));
    }

    @Override
    public List<Reservation> findByMemberAndThemeAndVisitDateBetween(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
    ) {
        return data.values().stream()
            .filter(reservation -> reservation.getTheme().getId().equals(themeId))
            .filter(reservation -> reservation.getMember().getId().equals(memberId))
            .filter(reservation -> reservation.getDate().isAfter(dateFrom) &&
                reservation.getDate().isBefore(dateTo))
            .toList();
    }

    @Override
    public List<Reservation> findAllByMember(Member member) {
        return data.values().stream()
            .filter(reservation -> reservation.getMember().equals(member))
            .toList();
    }
}
