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
import roomescape.global.handler.exception.NotFoundException;
import roomescape.global.handler.exception.ValidationException;
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
            throw new ValidationException(String.format("해당 (날짜: %s / 테마: %s)에 회원님에 대한 예약이 이미 존재합니다.",
                    date.toString(), theme.getName()));
        }
    }

    private void validateIsTimeSlotAlreadyReserved(LocalDate date, ReservationTime reservationTime, Theme theme) {
        if (reservationRepository.existsBySchedule_DateAndSchedule_TimeAndSchedule_Theme(date, reservationTime,
                theme)) {
            throw new ValidationException(String.format("해당 (날짜: %s / 시간: %s / 테마: %s)에 이미 예약이 존재합니다.",
                    date.toString(), reservationTime.getStartAt().toString(), theme.getName()));
        }
    }

    private void validateIsReservationInThePast(LocalDate date, ReservationTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new ValidationException("이미 지나간 시점을 예약할 수 없습니다.");
        }
    }

    private void validateMemberWaitingAlreadyExist(LocalDate date, Theme theme, Member member) {
        if (waitingRepository.existsBySchedule_DateAndSchedule_ThemeAndMember(date, theme, member)) {
            throw new ValidationException(String.format("해당 (날짜: %s / 테마: %s)에 회원님에 대한 예약대기가 이미 존재하여 예약할 수 없습니다.",
                    date.toString(), theme.getName()));
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
                .orElseThrow(() -> new NotFoundException(String.format("id값: %d 에 대한 예약이 존재하지 않습니다.", id)));

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
                .orElseThrow(() -> new NotFoundException(String.format("id값: %d 에 대한 테마가 존재하지 않습니다.", id)));
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("id값: %d 에 대한 예약시간이 존재하지 않습니다.", id)));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("id값: %d 에 대한 멤버가 존재하지 않습니다.", id)));
    }
}
