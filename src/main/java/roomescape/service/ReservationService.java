package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.exception.*;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              MemberRepository memberRepository,
                              ThemeRepository themeRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResult create(CreateReservationParam createReservationParam, LocalDateTime currentDateTime) {
        ReservationTime reservationTime = getReservationTimeFromRepository(createReservationParam.timeId());
        Theme theme = getThemeFromRepository(createReservationParam.themeId());
        Member member = getMemberFromRepository(createReservationParam.memberId());

        validateDuplicateReservation(createReservationParam, reservationTime, theme);
        validateReservationDateTime(createReservationParam, currentDateTime, reservationTime);

        Reservation reservation = Reservation.createNew(member, createReservationParam.date(), reservationTime, theme);
        reservationRepository.save(reservation);
        return ReservationResult.from(reservation);
    }

    @Transactional
    public void deleteByIdAndApproveFirstWaiting(Long reservationId) {
        Reservation reservation = getReservationFromRepository(reservationId);
        reservationRepository.deleteById(reservationId);
        Schedule schedule = reservation.getSchedule();
        Waiting waiting = getWaitingFromRepository(schedule);

        Reservation newReservation = Reservation.createNew(waiting.getMember(), reservation.getDate(), reservation.getTime(), reservation.getTheme());
        reservationRepository.save(newReservation);
    }

    public List<ReservationResult> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public ReservationResult findById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundReservationException(reservationId + "에 해당하는 reservation 튜플이 없습니다."));
        return ReservationResult.from(reservation);
    }

    public List<ReservationResult> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findReservationsInConditions(memberId, themeId, dateFrom, dateTo);
        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> findReservationsByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        return ReservationResult.from(reservations);
    }

    private Waiting getWaitingFromRepository(final Schedule schedule) {
        return waitingRepository.findTopByScheduleOrderByCreatedAt(schedule)
                .orElseThrow(() -> new NotFoundWaitingException("해당하는 예약 대기를 찾을 수 없습니다."));
    }

    private Reservation getReservationFromRepository(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow(
                () -> new NotFoundReservationException(reservationId + "에 해당하는 정보가 없습니다."));
    }

    private Member getMemberFromRepository(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new NotFoundWaitingException(memberId + "에 해당하는 멤버 정보가 없습니다."));
    }

    private ReservationTime getReservationTimeFromRepository(Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId).orElseThrow(
                () -> new NotFoundReservationTimeException(reservationTimeId + "에 해당하는 시간 정보가 없습니다."));
    }

    private Theme getThemeFromRepository(Long themeId) {
        return themeRepository.findById(themeId).orElseThrow(
                () -> new NotFoundThemeException(themeId + "에 해당하는 테마 정보가 없습니다."));
    }

    private void validateDuplicateReservation(final CreateReservationParam createReservationParam, final ReservationTime reservationTime, final Theme theme) {
        if (reservationRepository.existsBySchedule(new Schedule(createReservationParam.date(), reservationTime, theme))) {
            throw new UnAvailableReservationException("테마에 대해 날짜와 시간이 중복된 예약이 존재합니다.");
        }
    }

    private void validateReservationDateTime(final CreateReservationParam createReservationParam, final LocalDateTime currentDateTime, final ReservationTime reservationTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(createReservationParam.date(), reservationTime.getStartAt());
        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new UnAvailableReservationException("지난 날짜와 시간에 대한 예약은 불가능합니다.");
        }
        Duration duration = Duration.between(currentDateTime, reservationDateTime);
        if (duration.toMinutes() < 10) {
            throw new UnAvailableReservationException("예약 시간까지 10분도 남지 않아 예약이 불가합니다.");
        }
    }
}
