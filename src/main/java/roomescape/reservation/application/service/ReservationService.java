package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.datetime.CurrentDateTime;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationMineInfo;
import roomescape.reservation.application.dto.ReservationSearchCondition;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotRepository;
import roomescape.waiting.application.dto.WaitingInfo;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private static final LocalDate MINIMUM_SEARCH_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAXIMUM_SEARCH_DATE = LocalDate.of(2999, 12, 31);

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final CurrentDateTime currentDateTime;

    @Transactional
    public ReservationInfo createReservation(final ReservationCreateCommand command) {
        final Reservation reservation = makeReservation(command);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationInfo(savedReservation);
    }

    private Reservation makeReservation(final ReservationCreateCommand command) {
        final TimeSlot timeSlot = findTimeSlot(command.timeId());
        final Theme theme = findTheme(command.themeId());
        final Member member = findMember(command.memberId());
        final Reservation reservation = command.convertToEntity(timeSlot, theme, member);
        validateReservation(reservation);
        return reservation;
    }

    private void validateReservation(final Reservation reservation) {
        validateDuplicateReservation(reservation);
        validatePastReservation(reservation);
    }

    private void validateDuplicateReservation(final Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(reservation.date(), reservation.time().id(), reservation.theme().id())) {
            throw new RoomescapeException("이미 예약한 슬롯에 예약 할 수 없습니다.");
        }
    }

    private void validatePastReservation(final Reservation reservation) {
        if (reservation.isPast(currentDateTime.getDate(), currentDateTime.getTime())) {
            throw new RoomescapeException("이미 지난 슬롯에 예약 대기를 할 수 없습니다.");
        }
    }

    @Transactional
    public void cancelReservationById(final long id) {
        final Reservation reservation = findReservation(id);
        if (waitingRepository.existsByReservationId(reservation.id())) {
            final Waiting waiting = waitingRepository.findTopByReservationId(reservation.id())
                    .orElseThrow(() -> new RoomescapeException("예약 대기가 존재하지 않습니다."));
            final Reservation promotedReservation = new Reservation(waiting.reservation().date(), waiting.reservation().time(), waiting.reservation().theme(), waiting.member());
            reservationRepository.save(promotedReservation);
            waitingRepository.deleteById(waiting.id());
        }
        reservationRepository.deleteById(id);
    }

    public List<ReservationInfo> findReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationInfo::new)
                .toList();
    }

    public List<ReservationInfo> findReservationsBySearchCondition(final ReservationSearchCondition condition) {
        final LocalDate fromDate = Optional.ofNullable(condition.fromDate()).orElse(MINIMUM_SEARCH_DATE);
        final LocalDate toDate = Optional.ofNullable(condition.toDate()).orElse(MAXIMUM_SEARCH_DATE);
        return reservationRepository.findAllByCondition(condition.memberId(), condition.themeId(), fromDate, toDate)
                .stream()
                .map(ReservationInfo::new)
                .toList();
    }

    public List<ReservationMineInfo> findReservationsByMemberId(final long memberId) {
        final List<ReservationInfo> reservationInfos = reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(ReservationInfo::new)
                .toList();
        final List<WaitingInfo> waitingInfos = waitingRepository.findAllWithRankByMemberId(memberId)
                .stream()
                .map(WaitingInfo::new)
                .toList();

        return Stream.concat(
                reservationInfos.stream().map(ReservationMineInfo::new),
                waitingInfos.stream().map(ReservationMineInfo::new)
        ).sorted(Comparator.comparing(ReservationMineInfo::date)).toList();
    }

    private Reservation findReservation(final long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException("예약이 존재하지 않습니다."));
    }

    private TimeSlot findTimeSlot(final long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new RoomescapeException("예약 시간이 존재하지 않습니다."));
    }

    private Theme findTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException("테마가 존재하지 않습니다."));
    }

    private Member findMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException("멤버가 존재하지 않습니다."));
    }
}
