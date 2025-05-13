package roomescape.reservation.business.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.business.domain.Reservation;

public interface ReservationDao extends JpaRepository<Reservation, Long> {

    List<Reservation> findReservationByDateAndTheme_Id(LocalDate date, Long themeId);

    List<Reservation> findReservationByTheme_IdAndMember_IdAndDateBetween(long themeId, long memberId, LocalDate start,
                                                                          LocalDate end);

    boolean existsReservationById(Long id);

    boolean existsReservationByTime_Id(Long timeId);

    boolean existsReservationByTheme_Id(Long themeId);

//    List<Reservation> findAll();
//
//    Reservation save(Reservation reservation);
//
//    int deleteById(Long id);
//
//    Optional<Reservation> findById(Long id);
//
//    boolean existByTimeId(Long timeId);
//
//    boolean existByThemeId(Long themeId);
//
//    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);
//
//    List<Reservation> findReservationByThemeIdAndMemberIdInDuration(long themeId, long memberId, LocalDate start,
//                                                                    LocalDate end);
}
