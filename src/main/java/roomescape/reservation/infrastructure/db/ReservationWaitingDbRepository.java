package roomescape.reservation.infrastructure.db;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.reservation.infrastructure.db.dao.ReservationWaitingJpaRepository;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.repository.dto.ReservationWaitingWithRank;

@Repository
@RequiredArgsConstructor
public class ReservationWaitingDbRepository implements ReservationWaitingRepository {

    private final ReservationWaitingJpaRepository reservationWaitingJpaRepository;
    @Override
    public void save(ReservationWaiting reservationWaiting) {
        reservationWaitingJpaRepository.save(reservationWaiting);
    }

    @Override
    public List<ReservationWaitingWithRank> findAllWithRankByMemberId(Long memberId) {
        return reservationWaitingJpaRepository.findAllWithRankByMemberId(memberId);
    }

    @Override
    public ReservationWaiting getById(Long reservationWaitingId) {
        return reservationWaitingJpaRepository.findById(reservationWaitingId)
                .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 웨이팅이 존재하지 않습니다."));
    }

    @Override
    public Optional<ReservationWaiting> findFirstByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservationWaitingJpaRepository.findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAtAsc(date, timeId, themeId);
    }

    @Override
    public List<ReservationWaiting> getAll() {
        return reservationWaitingJpaRepository.findAll();
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId,
            Long memberId) {
        return reservationWaitingJpaRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, themeId, themeId,
                memberId);
    }

    @Override
    public void remove(ReservationWaiting reservationWaiting) {
        reservationWaitingJpaRepository.delete(reservationWaiting);
    }
}
