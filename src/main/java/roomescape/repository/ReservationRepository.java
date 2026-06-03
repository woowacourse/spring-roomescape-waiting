package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    Reservation updateReserver(Reservation reservation);

    boolean existsByTimeId(Long timeId);

    boolean existsBySlot(Slot slot);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findBySlot(Slot slot);

    List<Long> findReservedTimeIdsByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByMember(Member member);

    List<Reservation> findAll(int offset, int limit);

    long count();

    void deleteById(Long id);
}
