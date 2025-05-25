package roomescape.reservation.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.reservation.application.dto.ReservationIdWithSequenceResponse;
import roomescape.reservation.domain.QReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JdbcTemplateReservationRepository jdbcTemplateReservationRepository;
    private final JpaReservationRepository jpaReservationRepository;
    private final JPAQueryFactory queryFactory;

    private final QReservation reservation = QReservation.reservation;

    @Override
    public boolean existsById(final ReservationId id) {
        return jpaReservationRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsBySlot(final ReservationSlot slot) {
        return queryFactory
                       .selectOne()
                       .from(reservation)
                       .where(
                               slotEquals(slot)
                       )
                       .fetchFirst() != null;
    }

    @Override
    public boolean existsBySlotAndUserId(final ReservationSlot slot, final UserId userId) {
        return queryFactory
                       .selectOne()
                       .from(reservation)
                       .where(
                               slotEquals(slot),
                               reservation.userId.eq(userId)
                       )
                       .fetchFirst() != null;
    }

    @Override
    public Optional<Reservation> findById(final ReservationId id) {
        return jpaReservationRepository.findById(id.getValue());
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll();
    }

    @Override
    public List<Reservation> findAllByUserId(final UserId userId) {
        return jpaReservationRepository.findAllByUserId(userId);
    }

    @Override
    public Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(final ReservationDate startDate, final ReservationDate endDate, final int count) {
        return jdbcTemplateReservationRepository.findThemesToBookedCountByParamsOrderByBookedCount(startDate, endDate, count);
    }

    @Override
    public List<Reservation> findAllByParams(final UserId userId, final ThemeId themeId, final ReservationDate from, final ReservationDate to) {
        return jdbcTemplateReservationRepository.findAllByParams(userId, themeId, from, to);
    }

    @Override
    public List<ReservationIdWithSequenceResponse> findAllReservationSequencesByIds(final List<ReservationId> ids) {
        return jpaReservationRepository.findSequenceOfSlotByIds(
                        ids.stream().map(ReservationId::getValue).toList()).stream()
                .map(projection -> new ReservationIdWithSequenceResponse(
                        ReservationId.from(projection.getReservationId()), projection.getSlotRank()
                ))
                .toList();
    }

    @Override
    public Optional<Reservation> findNextBySlotAndCreatedAt(final ReservationSlot slot,
                                                            final LocalDateTime createdAt) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(reservation)
                        .where(
                                slotEquals(slot),
                                reservation.createdAt.gt(createdAt)
                        )
                        .fetchFirst());
    }

    @Override
    public Reservation save(final Reservation reservation) {
        return jpaReservationRepository.save(reservation);
    }

    @Override
    public void deleteById(final ReservationId id) {
        jpaReservationRepository.deleteById(id.getValue());
    }

    @Override
    public void delete(final Reservation reservation) {
        jpaReservationRepository.delete(reservation);
    }

    private BooleanExpression slotEquals(final ReservationSlot slot) {
        return reservation.date.eq(slot.getDate())
                .and(reservation.time.eq(slot.getTime()))
                .and(reservation.theme.eq(slot.getTheme()));
    }
}
