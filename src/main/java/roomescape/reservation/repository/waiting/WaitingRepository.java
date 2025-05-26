package roomescape.reservation.repository.waiting;

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
public class WaitingRepository implements WaitingRepositoryInterface {

    private final JpaWaitingRepository jpaWaitingRepository;

    @Override
    public Waiting save(final Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme) {
        return jpaWaitingRepository.existsByDateAndTimeAndTheme(date, time, theme);
    }

    @Override
    public List<Waiting> findByMember(final Member member) {
        return jpaWaitingRepository.findByMember(member);
    }

    @Override
    public Optional<Waiting> findById(final Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public void deleteById(final Long id) {
        jpaWaitingRepository.deleteById(id);
    }

    @Override
    public long countBefore(final Theme theme, final LocalDate date, final ReservationTime time, final Long id) {
        return jpaWaitingRepository.countBefore(theme, date, time, id);
    }

    @Override
    public List<Waiting> findAll() {
        return jpaWaitingRepository.findAll();
    }

    @Override
    public Optional<Waiting> findFirstByThemeAndDateAndTimeOrderByIdAsc(
            final Theme theme,
            final LocalDate date,
            final ReservationTime time) {
        return jpaWaitingRepository.findFirstByThemeAndDateAndTimeOrderByIdAsc(theme, date, time);
    }
}
