package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByDateAndTime_IdAndTheme_IdAndMember_Id(LocalDate date, Long timeId, Long themeId, Long memberId);

    boolean existsByDateAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);

    boolean existsByTime_Id(Long timeId);

    boolean existsByTheme_Id(Long themeId);

    boolean existsByIdAndMember_Id(Long id, Long memberId);

    List<Reservation> findByMember(Member member);

    List<Reservation> findByStatus(ReservationStatus reservationStatus);

    List<Reservation> findByDateAndTheme_Id(LocalDate date, Long themeId);

    List<Reservation> findAllByTheme_IdAndMember_IdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                  LocalDate dateTo);

    List<Reservation> findByDateAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);

    Optional<Reservation> findFirstByDateAndTime_IdAndTheme_IdAndStatus(LocalDate date, Long timeId, Long themeId, ReservationStatus status);
}
