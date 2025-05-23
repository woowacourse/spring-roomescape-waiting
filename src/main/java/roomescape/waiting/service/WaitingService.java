package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.reservationTime.entity.ReservationTime;
import roomescape.reservationTime.repository.JpaReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.JpaThemeRepository;
import roomescape.waiting.controller.dto.request.WaitingRequest;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;
import roomescape.waiting.repository.JpaWaitingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WaitingService {

    private final JpaWaitingRepository jpaWaitingRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;

    public WaitingService(
            final JpaWaitingRepository jpaWaitingRepository,
            final JpaReservationTimeRepository reservationTimeRepository,
            final JpaThemeRepository themeRepository
    ) {
        this.jpaWaitingRepository = jpaWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<WaitingWithRank> findAllWaiting() {
        return jpaWaitingRepository.findAllWaitingWithRank();
    }

    public List<WaitingWithRank> findAllWaitingWithRankByMemberId(final Member member) {
        return jpaWaitingRepository.findWaitingsWithRankByMemberId(member.getId());
    }

    @Transactional
    public Waiting addWaiting(final Member member, WaitingRequest request) {
        LocalDate date = request.date();

        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("theme"));

        validateDateTimeAfterNow(date, time);
        return jpaWaitingRepository.save(new Waiting(member, date, time, theme));
    }

    private void validateDateTimeAfterNow(final LocalDate date,final ReservationTime time) {
        LocalDateTime now = LocalDateTime.now();

        if (date.isBefore(now.toLocalDate()) ||
                (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new InvalidInputException("과거 예약은 불가능");
        }
    }

    public Optional<Waiting> removeWaiting(
            final Theme theme,
            final LocalDate date,
            final ReservationTime time
    ) {
        return jpaWaitingRepository
                .findFirstByThemeAndDateAndTimeOrderByIdAsc(theme, date, time)
                .map(waiting -> {
                    jpaWaitingRepository.delete(waiting);
                    return waiting;
                });
    }
}
