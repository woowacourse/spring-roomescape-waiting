package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    Optional<List<Reservation>> findByThemeId(Long themeId);

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findByMemberId(Long memberId);

    Optional<Reservation> findByDateAndTimeAndThemeAndMember(
            LocalDate date,
            ReservationTime time,
            RoomTheme theme,
            Member member);
}
