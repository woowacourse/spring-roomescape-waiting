package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import roomescape.CurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberDao;
import roomescape.reservation.service.dto.ReservationSearchCondition;
import roomescape.reservation.service.dto.ReservationCreateCommand;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.repository.ThemeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@Service
public class ReservationService {

    private static final LocalDate MINIMUM_SEARCH_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAXIMUM_SEARCH_DATE = LocalDate.of(2999, 12, 31);

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao;
    private final CurrentDateTime currentDateTime;

    public ReservationService(final ReservationDao reservationDao, final ReservationTimeDao reservationTimeDao,
                              final ThemeDao themeDao, final MemberDao memberDao,
                              final CurrentDateTime dateTimeGenerator) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
        this.currentDateTime = dateTimeGenerator;
    }

    public ReservationInfo createReservation(final ReservationCreateCommand command) {
        final Reservation reservation = makeReservation(command);
        if (reservation.isBefore(currentDateTime.getDateTime())) {
            throw new IllegalArgumentException("지나간 날짜와 시간은 예약 불가합니다.");
        }
        if (reservationDao.isExistsByDateAndTimeIdAndThemeId(command.date(), command.timeId(), command.themeId())) {
            throw new IllegalArgumentException("해당 시간에 이미 예약이 존재합니다.");
        }
        final Reservation savedReservation = reservationDao.save(reservation);
        return new ReservationInfo(savedReservation);
    }

    public List<ReservationInfo> getReservations() {
        return reservationDao.findAll().stream()
                .map(ReservationInfo::new)
                .toList();
    }

    public List<ReservationInfo> getReservations(ReservationSearchCondition condition) {
        Predicate<Reservation> memberPredicate = reservationMemberPredicate(condition);
        Predicate<Reservation> themePredicate = reservationThemePredicate(condition);
        Predicate<Reservation> datePredicate = reservationDatePredicate(condition);
        Predicate<Reservation> totalPredicate = memberPredicate.and(themePredicate).and(datePredicate);
        return reservationDao.findAll().stream()
                .filter(totalPredicate)
                .map(ReservationInfo::new)
                .toList();
    }

    private Predicate<Reservation> reservationMemberPredicate(ReservationSearchCondition condition) {
        if (condition.memberId() == null) {
            return reservation -> true;
        }
        return reservation -> reservation.isMemberHasSameId(condition.memberId());
    }

    private Predicate<Reservation> reservationThemePredicate(ReservationSearchCondition condition) {
        if (condition.themeId() == null) {
            return reservation -> true;
        }
        return reservation -> reservation.isThemeHasSameId(condition.themeId());
    }

    private Predicate<Reservation> reservationDatePredicate(ReservationSearchCondition condition) {
        LocalDate fromDate = Optional.ofNullable(condition.fromDate()).orElse(MINIMUM_SEARCH_DATE);
        LocalDate toDate = Optional.ofNullable(condition.toDate()).orElse(MAXIMUM_SEARCH_DATE);
        return reservation -> reservation.isBetween(fromDate, toDate);
    }

    public void cancelReservationById(final long id) {
        reservationDao.deleteById(id);
    }

    private Reservation makeReservation(final ReservationCreateCommand request) {
        final Member member = findMember(request.memberId());
        final ReservationTime reservationTime = findReservationTime(request.timeId());
        final Theme theme = findTheme(request.themeId());
        return request.convertToReservation(member, reservationTime, theme);
    }

    private Member findMember(final long memberId) {
        return memberDao.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
    }

    private ReservationTime findReservationTime(final long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 존재하지 않습니다."));
    }

    private Theme findTheme(final long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
    }
}
