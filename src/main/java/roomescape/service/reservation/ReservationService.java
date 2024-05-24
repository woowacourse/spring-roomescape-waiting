package roomescape.service.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.global.handler.exception.CustomException;
import roomescape.global.handler.exception.ExceptionCode;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.reservation.dto.request.ReservationConditionRequest;
import roomescape.service.reservation.dto.request.ReservationRequest;
import roomescape.service.reservation.dto.response.MyReservationResponse;
import roomescape.service.reservation.dto.response.ReservationResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository, WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse createReservation(ReservationRequest reservationRequest) {
        Member member = getMember(reservationRequest.memberId());
        ReservationTime reservationTime = getReservationTime(reservationRequest.timeId());
        Theme theme = getTheme(reservationRequest.themeId());

        validateIsTimeSlotAlreadyReserved(reservationRequest.date(), reservationTime, theme);
        validateIsReservationInThePast(reservationRequest.date(), reservationTime);
        validateMemberAlreadyReservedThemeOnDate(reservationRequest.date(), theme, member);
        validateMemberWaitingAlreadyExist(reservationRequest.date(), theme, member);

        Reservation reservation = reservationRequest.toEntity(member, reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateMemberAlreadyReservedThemeOnDate(LocalDate date, Theme theme, Member member) {
        if (reservationRepository.existsBySchedule_DateAndSchedule_ThemeAndMember(date, theme, member)) {
            throw new CustomException(ExceptionCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateIsTimeSlotAlreadyReserved(LocalDate date, ReservationTime reservationTime, Theme theme) {
        if (reservationRepository.existsBySchedule_DateAndSchedule_TimeAndSchedule_Theme(date, reservationTime, theme)) {
            throw new CustomException(ExceptionCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateIsReservationInThePast(LocalDate date, ReservationTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new CustomException(ExceptionCode.PAST_TIME_SLOT_RESERVATION);
        }
    }

    private void validateMemberWaitingAlreadyExist(LocalDate date, Theme theme, Member member) {
        if (waitingRepository.existsBySchedule_DateAndSchedule_ThemeAndMember(date, theme, member)) {
            throw new CustomException(ExceptionCode.ALREADY_WAITING_EXIST);
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
        List<Reservation> reservations = reservationRepository.findAllBySchedule_ThemeAndMemberAndSchedule_DateBetween(
                findTheme, findMember, condition.dateFrom(), condition.dateTo());

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findAllByMemberId(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<MyReservationResponse> reservationResponses = new java.util.ArrayList<>(
                reservationRepository.findAllByMember(member).stream()
                        .map(MyReservationResponse::from)
                        .toList());

        List<MyReservationResponse> waitingResponses = waitingRepository.findWaitingsWithRankByMemberId(id).stream()
                .map(MyReservationResponse::from)
                .toList();

        reservationResponses.addAll(waitingResponses);
        return reservationResponses;
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION));

        Optional<Waiting> waiting = waitingRepository.findFirstBySchedule_DateAndSchedule_ThemeAndSchedule_Time(
                reservation.getDate(), reservation.getTheme(),
                reservation.getTime());
        convertFirstWaitingToReservation(waiting);

        reservationRepository.deleteById(id);
    }

    private void convertFirstWaitingToReservation(Optional<Waiting> waiting) {
        if (waiting.isPresent()) {
            waiting.ifPresent(waitingRepository::delete);
            reservationRepository.save(waiting.get().toReservation());
        }
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
