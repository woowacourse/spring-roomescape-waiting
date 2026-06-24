package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Reservation update(Reservation reservation);

    boolean existsByTimeId(Long timeId);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByIdForUpdate(Long id);

    Optional<Reservation> findBySlot(Slot slot);

    List<Long> findReservedTimeIdsByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findByMember(Member member);

    List<Reservation> findAll(int offset, int limit);

    long count();

    void deleteById(Long id);
}
