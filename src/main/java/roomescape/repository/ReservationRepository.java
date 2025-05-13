package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Reservation save(Reservation reservation); //TODO: 이미 구현되어있어서 나중에 삭제

    List<Reservation> findAll(); //TODO: 이미 구현되어있어서 나중에 삭제

    Optional<Reservation> findById(long addedReservationId); //TODO: 이미 구현되어있어서 나중에 삭제

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId); //TODO: 테마 확인 필요

    List<Reservation> findAllByDateInRange(LocalDate start, LocalDate end); //TODO: 메서드 작성 규칙 확인 필요

    List<Reservation> findAllByFilter(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo); //TODO: 동적쿼리

    boolean existsByDateAndTimeIdAndTheme(Reservation reservation); //TODO: 파라미터 변경 필요

    void deleteById(Long id); //TODO: 이미 구현되어있어서 나중에 삭제
}
