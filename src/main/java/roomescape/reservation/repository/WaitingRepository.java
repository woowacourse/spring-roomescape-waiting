package roomescape.reservation.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import roomescape.reservation.domain.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    );
}
