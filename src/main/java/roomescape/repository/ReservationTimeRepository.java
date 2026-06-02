package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservedTimeResponseDTO;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    void delete(Long id);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    List<ReservedTimeResponseDTO> findReservedTimes(LocalDate date, Long themeId);

    boolean existByStartAt(LocalTime startAt);
}
