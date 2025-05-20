package roomescape.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.QMember;
import roomescape.domain.QReservation;
import roomescape.domain.QReservationTheme;
import roomescape.domain.QReservationTime;
import roomescape.domain.Reservation;

@RequiredArgsConstructor
@Repository
public class ReservationRepositoryCustomImpl implements ReservationRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(Long memberId, Long themeId,
                                                                          LocalDate dateFrom, LocalDate dateTo) {
        QReservation reservation = QReservation.reservation;
        QMember member = QMember.member;
        QReservationTime time = QReservationTime.reservationTime;
        QReservationTheme theme = QReservationTheme.reservationTheme;

        return queryFactory
                .selectFrom(reservation)
                .innerJoin(reservation.member, member).fetchJoin()
                .innerJoin(reservation.time, time).fetchJoin()
                .innerJoin(reservation.theme, theme).fetchJoin()
                .where(
                        memberIdEq(memberId),
                        themeIdEq(themeId),
                        dateFromGoe(dateFrom),
                        dateToLoe(dateTo)
                )
                .fetch();
    }
    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? QReservation.reservation.member.id.eq(memberId) : null;
    }

    private BooleanExpression themeIdEq(Long themeId) {
        return themeId != null ? QReservation.reservation.theme.id.eq(themeId) : null;
    }

    private BooleanExpression dateFromGoe(LocalDate dateFrom) {
        return dateFrom != null ? QReservation.reservation.date.goe(dateFrom) : null;
    }

    private BooleanExpression dateToLoe(LocalDate dateTo) {
        return dateTo != null ? QReservation.reservation.date.loe(dateTo) : null;
    }
}
