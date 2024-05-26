package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationEntry;
import roomescape.domain.WaitingWithRank;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.request.ReservationConditionRequest;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.MyReservationEntryResponse;
import roomescape.service.dto.response.ReservationResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;
    private final WaitingService waitingService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository, final WaitingRepository waitingRepository,
                              final WaitingService waitingService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
        this.waitingService = waitingService;
    }

    public ReservationResponse createReservation(ReservationRequest reservationRequest, Long memberId) {
        Member member = getMember(memberId);
        ReservationTime reservationTime = getReservationTime(reservationRequest.timeId());
        Theme theme = getTheme(reservationRequest.themeId());

        if (reservationRepository.existsByDateAndTime(reservationRequest.date(), reservationTime)) {
            throw new CustomException(ExceptionCode.DUPLICATE_RESERVATION);
        }
        validateIsPastTime(reservationRequest.date(), reservationTime);

        Reservation reservation = reservationRequest.toEntity(member, reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateIsPastTime(LocalDate date, ReservationTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new CustomException(ExceptionCode.PAST_TIME_SLOT_RESERVATION);
        }
    }

    public List<ReservationResponse> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllReservationsByCondition(ReservationConditionRequest condition) {
        Theme findTheme = getTheme(condition.themeId());
        Member findMember = getMember(condition.memberId());
        List<Reservation> reservations = reservationRepository.findAllByMemberAndThemeAndDateBetween(
                findMember, findTheme,
                condition.dateFrom(), condition.dateTo());

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationEntryResponse> findAllByMemberId(Long id) {
        Member member = getMember(id);
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        List<WaitingWithRank> waitingWithRanks = waitingService.findAllWithRankByMember(member);

        List<ReservationEntry> reservationEntries = ReservationEntry.createEntries(reservations, waitingWithRanks);

        return reservationEntries.stream()
                .map(MyReservationEntryResponse::from)
                .toList();
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION));

        if (!waitingRepository.existsByReservation(reservation)) {
            reservationRepository.deleteById(id);
        }
        waitingService.convertWaitingToReservation(reservation);
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_THEME));
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION_TIME));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_USER));
    }
}
