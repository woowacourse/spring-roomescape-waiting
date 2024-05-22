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
import roomescape.domain.reservation.dto.BookableTimeResponse;
import roomescape.domain.reservation.dto.BookableTimesRequest;
import roomescape.domain.reservation.dto.ReservationAddRequest;
import roomescape.domain.reservation.dto.ReservationMineResponse;
import roomescape.domain.reservation.repository.reservation.ReservationRepository;
import roomescape.domain.reservation.repository.reservationTime.ReservationTimeRepository;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.global.exception.EscapeApplicationException;
import roomescape.global.exception.NoMatchingDataException;

@Service
public class ReservationService {

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

    public List<Reservation> findFilteredReservationList(Long themeId, Long memberId,
                                                         LocalDate dateFrom, LocalDate dateTo) {
        return reservationRepository.findAllBy(themeId, memberId, dateFrom, dateTo);
    }

    public Reservation addReservation(ReservationAddRequest reservationAddRequest) {
        validateDuplicatedReservation(reservationAddRequest);
        Reservation reservation = getReservationWithStatus(reservationAddRequest, RESERVED);
        validateReservationDateTime(reservation);
        return reservationRepository.save(reservation);
    }

    public Reservation addWaitingReservation(ReservationAddRequest reservationAddRequest) {
        validateDuplicatedWaitingReservation(reservationAddRequest);
        Reservation reservation = getReservationWithStatus(reservationAddRequest, WAITING);
        validateReservationDateTime(reservation);
        return reservationRepository.save(reservation);
    }

    private void validateDuplicatedReservation(ReservationAddRequest reservationAddRequest) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(reservationAddRequest.date(),
                reservationAddRequest.timeId(), reservationAddRequest.themeId())) {
            throw new EscapeApplicationException("예약 날짜와 예약시간 그리고 테마가 겹치는 예약은 할 수 없습니다.");
        }
    }

    private void validateDuplicatedWaitingReservation(ReservationAddRequest reservationAddRequest) {
        if (reservationRepository.existByMemberIdAndDateAndTimeIdAndThemeId(
                reservationAddRequest.memberId(), reservationAddRequest.date(),
                reservationAddRequest.timeId(), reservationAddRequest.themeId())) {
            throw new EscapeApplicationException("겹치는 예약대기 또는 예약은 할 수 없습니다.");
        }
    }

    private void validateReservationDateTime(Reservation reservation) {
        LocalDateTime reservationDateTime = reservation.getDate().atTime(reservation.getTime().getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new EscapeApplicationException(reservationDateTime + ": 예약은 현재 보다 이전일 수 없습니다");
        }
    }

    private Reservation getReservationWithStatus(ReservationAddRequest reservationAddRequest,
                                                 ReservationStatus reservationStatus) {
        ReservationTime reservationTime = getReservationTime(reservationAddRequest.timeId());
        Theme theme = getTheme(reservationAddRequest.themeId());
        Member member = getMember(reservationAddRequest.memberId());

        return reservationAddRequest.toEntity(reservationTime, theme, member, reservationStatus, LocalDateTime.now());
    }


    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoMatchingDataException("존재 하지 않는 테마로 예약할 수 없습니다"));
    }

    private ReservationTime getReservationTime(Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new NoMatchingDataException("존재 하지 않는 예약시각으로 예약할 수 없습니다."));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoMatchingDataException("존재 하지 않는 멤버로 예약할 수 없습니다."));
    }

    public List<BookableTimeResponse> findBookableTimes(BookableTimesRequest bookableTimesRequest) {
        List<Reservation> bookedReservations = reservationRepository.findByDateAndThemeId(bookableTimesRequest.date(),
                bookableTimesRequest.themeId());
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
        if (reservationRepository.findById(id).isEmpty()) {
            throw new NoMatchingDataException("해당 id를 가진 예약이 존재하지 않습니다.");
        }
        reservationRepository.deleteById(id);
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
