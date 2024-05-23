package roomescape.domain.reservation.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.domain.Reservation;
import roomescape.domain.reservation.domain.Status;
import roomescape.domain.reservation.dto.ReservationAddRequest;
import roomescape.domain.reservation.dto.ReservationMineResponse;
import roomescape.domain.reservation.dto.ReservationWaitAddRequest;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.domain.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.domain.ReservationTime;
import roomescape.domain.time.dto.BookableTimeResponse;
import roomescape.domain.time.dto.BookableTimesRequest;
import roomescape.domain.time.repository.ReservationTimeRepository;
import roomescape.global.exception.DataConflictException;

import java.time.LocalDate;
import java.util.List;

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
        return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo);
    }

    public Reservation addReservation(ReservationAddRequest reservationAddRequest) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndStatus(reservationAddRequest.date(),
                reservationAddRequest.timeId(), reservationAddRequest.themeId(), Status.RESERVATION)) {
            throw new DataConflictException("예약 날짜와 예약시간 그리고 테마가 겹치는 예약이 있으면 예약을 할 수 없습니다.");
        }

        if (reservationRepository.existsByMemberIdAndDateAndTimeIdAndThemeIdAndStatus(reservationAddRequest.memberId(),
                reservationAddRequest.date(), reservationAddRequest.timeId(), reservationAddRequest.themeId(), Status.RESERVATION_WAIT)) {
            throw new DataConflictException("멤버와 예약 날짜 그리고 예약시간, 테마가 겹치는 예약대기가 있으면 예약을 할 수 없습니다.");
        }

        ReservationTime reservationTime = getReservationTime(reservationAddRequest.timeId());
        Theme theme = getTheme(reservationAddRequest.themeId());
        Member member = getMember(reservationAddRequest.memberId());

        Reservation reservation = reservationAddRequest.toEntity(reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 테마로 예약할 수 없습니다"));
    }

    private ReservationTime getReservationTime(Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 예약시각으로 예약할 수 없습니다."));
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 멤버로 예약할 수 없습니다."));
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
            throw new EntityNotFoundException("해당 id를 가진 예약이 존재하지 않습니다.");
        }
        reservationRepository.deleteById(id);
    }

    public List<ReservationMineResponse> findReservationByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId).stream()
                .map(ReservationMineResponse::new)
                .toList();
    }

    public Reservation addReservationWait(ReservationWaitAddRequest reservationWaitAddRequest) {
        if (!reservationRepository.existsByDateAndTimeIdAndThemeIdAndStatus(reservationWaitAddRequest.date(),
                reservationWaitAddRequest.timeId(), reservationWaitAddRequest.themeId(), Status.RESERVATION)) {
            throw new DataConflictException("예약 날짜와 예약시간 그리고 테마가 겹치는 예약이 없으면 예약대기를 할 수 없습니다.");
        }

        if (reservationRepository.existsByMemberIdAndDateAndTimeIdAndThemeIdAndStatus(reservationWaitAddRequest.memberId(),
                reservationWaitAddRequest.date(), reservationWaitAddRequest.timeId(), reservationWaitAddRequest.themeId(), Status.RESERVATION_WAIT)) {
            throw new DataConflictException("멤버와 예약 날짜 그리고 예약시간, 테마가 겹치는 예약대기가 있으면 예약대기를 할 수 없습니다.");
        }

        ReservationTime reservationTime = getReservationTime(reservationWaitAddRequest.timeId());
        Theme theme = getTheme(reservationWaitAddRequest.themeId());
        Member member = getMember(reservationWaitAddRequest.memberId());

        Reservation reservation = reservationWaitAddRequest.toEntity(reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }

    public void removeReservationWait(Long id) {
        if (reservationRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("해당 id를 가진 예약대기가 존재하지 않습니다.");
        }
        reservationRepository.deleteById(id);
    }
}
