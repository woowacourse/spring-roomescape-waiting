package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    // TODO: 메서드명 너무 김
    List<Reservation> findByMember_IdAndTheme_IdAndDateBetween(Long memberId, Long themeId, LocalDate from, LocalDate to);

    // TODO: 언더스코어가 컨벤션에 맞지 않다
    boolean existsByTime_Id(long timeId);

    boolean existsByTheme_Id(long themeId);

    boolean existsByDateAndTime_IdAndTheme_Id(LocalDate date, Long timeId, Long themeId);
}
