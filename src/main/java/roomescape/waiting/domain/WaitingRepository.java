package roomescape.waiting.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    boolean existsByReservation(LocalDate date, long timeId, long themeId);

    boolean existsByReservationAndMemberId(LocalDate date, long timeId, long themeId, long memberId);

    Waiting save(Waiting waiting);

    void deleteById(long id);

    List<Waiting> findAll();

    List<WaitingWithRank> findAllWithRankByMemberId(long memberId);

    Optional<Waiting> findTopByReservation(LocalDate date, long timeId, long themeId);
}
