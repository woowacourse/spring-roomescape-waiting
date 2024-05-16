package roomescape.infrastructure;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import roomescape.domain.QReservation;
import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ReservationCustomRepositoryImpl implements ReservationCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final QReservation reservation = new QReservation("reservation");

    public List<Reservation> findAllWithSearchConditions(Long memberId, Long themeId, LocalDate from, LocalDate to) {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);
        return jpaQueryFactory.selectFrom(reservation)
                .where(equalsMemberId(memberId),
                        equalsThemeId(themeId),
                        greaterThanOrEqualsFrom(from),
                        lessThanOrEqualsTo(to))
                .fetch();
    }

    private BooleanExpression equalsMemberId(Long memberId) {
        if (memberId == null) {
            return Expressions.TRUE;
        }
        return reservation.member.id.eq(memberId);
    }

    private BooleanExpression equalsThemeId(Long themeId) {
        if (themeId == null) {
            return Expressions.TRUE;
        }
        return reservation.theme.id.eq(themeId);
    }

    private BooleanExpression greaterThanOrEqualsFrom(LocalDate from) {
        if (from == null) {
            return Expressions.TRUE;
        }
        return reservation.date.date.goe(from);
    }

    private BooleanExpression lessThanOrEqualsTo(LocalDate to) {
        if (to == null) {
            return Expressions.TRUE;
        }
        return reservation.date.date.loe(to);
    }
}
