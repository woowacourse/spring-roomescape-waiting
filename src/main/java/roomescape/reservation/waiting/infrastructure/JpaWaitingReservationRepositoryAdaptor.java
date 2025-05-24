package roomescape.reservation.waiting.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.waiting.domain.WaitingReservationRepository;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;

@Repository
public class JpaWaitingReservationRepositoryAdaptor implements WaitingReservationRepository {

    private final JpaWaitingReservationRepository jpaWaitingReservationRepository;

    public JpaWaitingReservationRepositoryAdaptor(JpaWaitingReservationRepository jpaWaitingReservationRepository) {
        this.jpaWaitingReservationRepository = jpaWaitingReservationRepository;
    }

    @Override
    public WaitingReservation save(WaitingReservation waitingReservation) {
        return jpaWaitingReservationRepository.save(waitingReservation);
    }

    @Override
    public Optional<WaitingReservation> findById(Long id) {
        return jpaWaitingReservationRepository.findById(id);
    }

    @Override
    public List<WaitingReservationWithRank> findWaitingsWithRankByMember_Id(Long memberId) {
        return jpaWaitingReservationRepository.findWaitingsWithRankByMember_Id(memberId);
    }

    @Override
    public List<WaitingReservation> findAll() {
        return jpaWaitingReservationRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaWaitingReservationRepository.deleteById(id);
    }

    @Override
    public void deleteByIdAndMemberId(Long id, Long memberId) {
        jpaWaitingReservationRepository.deleteByIdAndMemberId(id, memberId);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaWaitingReservationRepository.existsById(id);
    }

    @Override
    public boolean existsByIdAndMemberId(Long id, Long memberId) {
        return jpaWaitingReservationRepository.existsByIdAndMemberId(id, memberId);
    }

    @Override
    public boolean existsByThemeIdAndTimeIdAndDateAndMemberId(Long themeId, Long timeId, LocalDate date, Long memberId) {
        return jpaWaitingReservationRepository.existsByThemeIdAndTimeIdAndDateAndMemberId(themeId, timeId, date, memberId);
    }
}
