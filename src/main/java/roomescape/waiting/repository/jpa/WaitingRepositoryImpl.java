package roomescape.waiting.repository.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.exception.WaitingAlreadyExistsException;
import roomescape.waiting.repository.WaitingRepository;
import roomescape.waiting.repository.dto.WaitingWithRank;
import roomescape.waiting.repository.entity.WaitingEntity;

@Profile("jpa")
@Repository
@RequiredArgsConstructor
public class WaitingRepositoryImpl implements WaitingRepository {

    private static final String UNIQUE_CONSTRAINT_NAME = "unique_reservation_date_time_theme_name";

    private final WaitingJpaRepository waitingJpaRepository;

    @Override
    public Waiting save(final Waiting waiting) {
        try {
            WaitingEntity saved = waitingJpaRepository.saveAndFlush(WaitingEntity.from(waiting));
            return Waiting.of(
                saved.getId(),
                waiting.getCustomerNameValue(),
                waiting.getReservationDate(),
                saved.getCreatedAt(),
                waiting.getTime(),
                waiting.getTheme()
            );
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains(UNIQUE_CONSTRAINT_NAME)) {
                throw new WaitingAlreadyExistsException();
            }
            throw e;
        }
    }

    @Override
    public boolean deleteById(final long id) {
        if (!waitingJpaRepository.existsById(id)) {
            return false;
        }
        waitingJpaRepository.deleteById(id);
        return true;
    }

    @Override
    public Optional<Waiting> findById(final long id) {
        return waitingJpaRepository.findByIdWithJoins(id)
            .map(WaitingEntity::toDomain);
    }

    @Override
    public Optional<Waiting> findEarliestBySlotForUpdate(
        final LocalDate date,
        final long timeId,
        final long themeId
    ) {
        return waitingJpaRepository.findEarliestBySlotForUpdate(date, timeId, themeId)
            .map(WaitingEntity::toDomain);
    }

    @Override
    public List<WaitingWithRank> findAllWithRankByCustomerNameAndReservationDateTimeAfter(
        final String customerName,
        final LocalDateTime now
    ) {
        return waitingJpaRepository
            .findWithRankByCustomerNameAfter(customerName, now.toLocalDate(), now.toLocalTime().toString()).stream()
            .map(this::toWaitingWithRank)
            .toList();
    }

    @Override
    public boolean existsBySlot(final LocalDate reservationDate, final long timeId, final long themeId) {
        return waitingJpaRepository.existsByReservationDateAndTimeIdAndThemeId(reservationDate, timeId, themeId);
    }

    @Override
    public List<WaitingWithRank> findAllWithRank() {
        return waitingJpaRepository.findAllWithRankProjection().stream()
            .map(this::toWaitingWithRank)
            .toList();
    }

    private WaitingWithRank toWaitingWithRank(final WaitingRankProjection waitingRankProjection) {
        ReservationTime time = ReservationTime.of(
            waitingRankProjection.getTimeId(),
            waitingRankProjection.getTimeStartAt()
        );
        Theme theme = Theme.of(
            waitingRankProjection.getThemeId(),
            waitingRankProjection.getThemeName(),
            waitingRankProjection.getThemeDescription(),
            waitingRankProjection.getThemeThumbnailUrl()
        );

        Waiting waiting = Waiting.of(
            waitingRankProjection.getId(),
            waitingRankProjection.getCustomerName(),
            waitingRankProjection.getReservationDate(),
            waitingRankProjection.getCreatedAt(),
            time,
            theme
        );
        return new WaitingWithRank(waiting, waitingRankProjection.getRank());
    }
}
