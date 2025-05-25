package roomescape.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.QMember;
import roomescape.domain.reservation.QReservation;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationitem.QReservationItem;
import roomescape.domain.reservationitem.QReservationTheme;
import roomescape.domain.reservationitem.QReservationTime;

@RequiredArgsConstructor
@Repository
public class ReservationRepositoryCustomImpl implements ReservationRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(Long memberId, Long themeId,
                                                                          LocalDate dateFrom, LocalDate dateTo) {
        QReservation reservation = QReservation.reservation;
        QMember member = QMember.member;
        QReservationItem reservationItem = QReservationItem.reservationItem;
        QReservationTime time = QReservationTime.reservationTime;
        QReservationTheme theme = QReservationTheme.reservationTheme;

        return queryFactory
                .selectFrom(reservation)
                .innerJoin(reservation.member, member).fetchJoin()
                .innerJoin(reservation.reservationItem, reservationItem).fetchJoin()
                .innerJoin(reservation.reservationItem.time, time).fetchJoin()
                .innerJoin(reservation.reservationItem.theme, theme).fetchJoin()
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
        return themeId != null ? QReservation.reservation.reservationItem.theme.id.eq(themeId) : null;
    }

    private BooleanExpression dateFromGoe(LocalDate dateFrom) {
        return dateFrom != null ? QReservation.reservation.reservationItem.date.goe(dateFrom) : null;
    }

    private BooleanExpression dateToLoe(LocalDate dateTo) {
        return dateTo != null ? QReservation.reservation.reservationItem.date.loe(dateTo) : null;
    }
}
