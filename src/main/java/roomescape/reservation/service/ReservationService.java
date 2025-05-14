package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import roomescape.CurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.dto.ReservationSearchCondition;
import roomescape.reservation.service.dto.ReservationCreateCommand;
import roomescape.reservation.service.dto.ReservationInfo;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@Service
public class ReservationService {

    private static final LocalDate MINIMUM_SEARCH_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAXIMUM_SEARCH_DATE = LocalDate.of(2999, 12, 31);

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final CurrentDateTime currentDateTime;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository, final MemberRepository memberRepository,
                              final CurrentDateTime dateTimeGenerator) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.currentDateTime = dateTimeGenerator;
    }

    public ReservationInfo createReservation(final ReservationCreateCommand command) {
        final Reservation reservation = makeReservation(command);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationInfo(savedReservation);
    }

    private Reservation makeReservation(final ReservationCreateCommand command) {
        final ReservationTime reservationTime = findReservationTime(command.timeId());
        validatePastDateTime(command.date(), reservationTime);
        validateDuplicateReservation(command);
        final Member member = findMember(command.memberId());
        final Theme theme = findTheme(command.themeId());
        return command.convertToReservation(member, reservationTime, theme);
    }

    private void validatePastDateTime(final LocalDate date, final ReservationTime reservationTime) {
        final boolean isBefore = date.isBefore(currentDateTime.getDate()) ||
                date.isEqual(currentDateTime.getDate()) &&
                reservationTime.isBefore(currentDateTime.getTime());
        if (isBefore) {
            throw new IllegalArgumentException("지나간 날짜와 시간은 예약 불가합니다.");
        }
    }

    private void validateDuplicateReservation(ReservationCreateCommand command) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(command.date(), command.timeId(),
                command.themeId())) {
            throw new IllegalArgumentException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    private Member findMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
    }

    private ReservationTime findReservationTime(final long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 존재하지 않습니다."));
    }

    private Theme findTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
    }

    public List<ReservationInfo> getReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationInfo::new)
                .toList();
    }

    public List<ReservationInfo> getReservations(ReservationSearchCondition condition) {
        Predicate<Reservation> memberPredicate = reservationMemberPredicate(condition);
        Predicate<Reservation> themePredicate = reservationThemePredicate(condition);
        Predicate<Reservation> datePredicate = reservationDatePredicate(condition);
        Predicate<Reservation> totalPredicate = memberPredicate.and(themePredicate).and(datePredicate);
        return reservationRepository.findAll().stream()
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

    public List<ReservationInfo> findReservationsByMemberId(final Long id) {
        return reservationRepository.findAllByMemberId(id)
                .stream()
                .map(ReservationInfo::new)
                .toList();
    }

    public void cancelReservationById(final long id) {
        reservationRepository.deleteById(id);
    }
}
