package roomescape.domain.reservation.service;

import static roomescape.domain.reservation.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.domain.reservation.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.dto.command.ReservationAddCommand;
import roomescape.domain.reservation.dto.query.BookableTimesQuery;
import roomescape.domain.reservation.dto.query.ReservationSearchQuery;
import roomescape.domain.reservation.dto.response.BookableTimeResponse;
import roomescape.domain.reservation.dto.response.ReservationMineResponse;
import roomescape.domain.reservation.repository.reservation.ReservationRepository;
import roomescape.domain.reservation.repository.reservationTime.ReservationTimeRepository;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.global.exception.EscapeApplicationException;
import roomescape.global.exception.NoMatchingDataException;

@Service
public class ReservationService {

    protected static final String DUPLICATED_RESERVATION_ERROR_MESSAGE = "예약 날짜와 예약시간 그리고 테마가 겹치는 예약은 할 수 없습니다.";
    protected static final String DUPLICATED_RESERVATION_WAITING_ERROR_MESSAGE = "겹치는 예약대기 또는 예약은 할 수 없습니다.";
    protected static final String PAST_RESERVATION_ERROR_MESSAGE = ": 예약은 현재 보다 이전일 수 없습니다";
    protected static final String NON_EXIST_THEME_ERROR_MESSAGE = "존재 하지 않는 테마로 예약할 수 없습니다";
    protected static final String NON_EXIST_RESERVATION_TIME_ERROR_MESSAGE = "존재 하지 않는 예약시각으로 예약할 수 없습니다.";
    protected static final String NON_EXIST_MEMBER_ERROR_MESSAGE = "존재 하지 않는 멤버로 예약할 수 없습니다.";
    protected static final String NON_EXIST_RESERVATION_ID_ERROR_MESSAGE = "해당 id를 가진 예약이 존재하지 않습니다.";

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<Reservation> findAllReservation() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findFilteredReservationList(ReservationSearchQuery reservationSearchQuery) {
        return reservationRepository.findAllBy(
                reservationSearchQuery.themeId(),
                reservationSearchQuery.memberId(),
                reservationSearchQuery.dateFrom(),
                reservationSearchQuery.dateTo());
    }

    public Reservation addReservedReservation(ReservationAddCommand reservationAddCommand) {
        validateDuplicatedReservation(reservationAddCommand);
        Reservation reservation = getReservationWithStatus(reservationAddCommand, RESERVED);
        validateReservationDateTime(reservation);
        return reservationRepository.save(reservation);
    }

    public Reservation addWaitingReservation(ReservationAddCommand reservationAddCommand) {
        validateDuplicatedWaitingReservation(reservationAddCommand);
        Reservation reservation = getReservationWithStatus(reservationAddCommand, WAITING);
        validateReservationDateTime(reservation);
        return reservationRepository.save(reservation);
    }

    private void validateDuplicatedReservation(ReservationAddCommand reservationAddCommand) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(reservationAddCommand.date(),
                reservationAddCommand.timeId(), reservationAddCommand.themeId())) {
            throw new EscapeApplicationException(DUPLICATED_RESERVATION_ERROR_MESSAGE);
        }
    }

    private void validateDuplicatedWaitingReservation(ReservationAddCommand reservationAddCommand) {
        if (reservationRepository.existByMemberIdAndDateAndTimeIdAndThemeId(
                reservationAddCommand.memberId(),
                reservationAddCommand.date(),
                reservationAddCommand.timeId(),
                reservationAddCommand.themeId())) {
            throw new EscapeApplicationException(DUPLICATED_RESERVATION_WAITING_ERROR_MESSAGE);
        }
    }

    private void validateReservationDateTime(Reservation reservation) {
        LocalDateTime reservationDateTime = reservation.getDate().atTime(reservation.getTime().getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new EscapeApplicationException(reservationDateTime + PAST_RESERVATION_ERROR_MESSAGE);
        }
    }

    private Reservation getReservationWithStatus(ReservationAddCommand reservationAddCommand,
                                                 ReservationStatus reservationStatus) {
        ReservationTime reservationTime = getReservationTime(reservationAddCommand.timeId());
        Theme theme = getTheme(reservationAddCommand.themeId());
        Member member = getMember(reservationAddCommand.memberId());

        return reservationAddCommand.toEntity(reservationTime, theme, member, reservationStatus, LocalDateTime.now());
    }


    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoMatchingDataException(NON_EXIST_THEME_ERROR_MESSAGE));
    }

    private ReservationTime getReservationTime(Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new NoMatchingDataException(NON_EXIST_RESERVATION_TIME_ERROR_MESSAGE));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoMatchingDataException(NON_EXIST_MEMBER_ERROR_MESSAGE));
    }

    public List<BookableTimeResponse> findBookableTimes(BookableTimesQuery bookableTimesQuery) {
        List<Reservation> bookedReservations = reservationRepository.findByDateAndThemeId(
                bookableTimesQuery.date(),
                bookableTimesQuery.themeId());
        List<ReservationTime> bookedTimes = bookedReservations.stream()
                .map(Reservation::getTime)
                .toList();
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        return allTimes.stream()
                .map(time -> new BookableTimeResponse(time.getStartAt(), time.getId(), isBookedTime(bookedTimes, time)))
                .toList();
    }

    private boolean isBookedTime(List<ReservationTime> bookedTimes, ReservationTime time) {
        return bookedTimes.contains(time);
    }

    public void removeReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NoMatchingDataException(NON_EXIST_RESERVATION_ID_ERROR_MESSAGE));

        reservationRepository.deleteById(id);
        updateWaitingToReserved(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
    }

    private void updateWaitingToReserved(LocalDate date, Long timeId, Long themeId) {
        reservationRepository
                .findTopWaitingReservationBy(date, timeId, themeId)
                .ifPresent(waitingReservation ->
                        reservationRepository.save(waitingReservation.changeStatusToReserved()
                        )
                );
    }

    public List<ReservationMineResponse> findReservationByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(ReservationMineResponse::from)
                .toList();
    }

    public List<Reservation> findWaitingReservations() {
        return reservationRepository.findByStatus(WAITING);
    }
}
