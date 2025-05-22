package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.theme.domain.Theme;

@RequiredArgsConstructor
@Repository
public class JpaWaitingRepository implements WaitingRepositoryInterface {

    private final WaitingRepository waitingRepository;

    @Override
    public Waiting save(final Waiting waiting) {
        return waitingRepository.save(waiting);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme) {
        return waitingRepository.existsByDateAndTimeAndTheme(date, time, theme);
    }

    @Override
    public List<Waiting> findByMember(final Member member) {
        return waitingRepository.findByMember(member);
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return waitingRepository.findById(id);
    }

    @Override
    public void deleteById(final Long id) {
        waitingRepository.deleteById(id);
    }

    @Override
    public long countBefore(final Theme theme, final LocalDate date, final ReservationTime time, final Long id) {
        return waitingRepository.countBefore(theme, date, time, id);
    }
}
