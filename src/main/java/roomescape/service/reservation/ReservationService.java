package roomescape.service.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Schedule;
import roomescape.domain.reservation.ScheduleRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.exception.UnauthorizedException;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFindRequest;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationResponse;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository, ScheduleRepository scheduleRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public ReservationResponse create(AdminReservationRequest adminReservationRequest) {
        return createReservation(adminReservationRequest.timeId(), adminReservationRequest.themeId(),
                adminReservationRequest.memberId(), adminReservationRequest.date());
    }

    public ReservationResponse create(ReservationRequest reservationRequest, long memberId) {
        return createReservation(reservationRequest.timeId(), reservationRequest.themeId(), memberId,
                reservationRequest.date());
    }

    private ReservationResponse createReservation(long timeId, long themeId, long memberId, LocalDate date) {
        ReservationDate reservationDate = ReservationDate.of(date);
        ReservationTime reservationTime = findTimeById(timeId);
        Theme theme = findThemeById(themeId);
        Member member = findMemberById(memberId);
        validate(reservationDate, reservationTime, theme);
        Schedule schedule = getScheduleOf(reservationDate, reservationTime);
        Reservation reservation = reservationRepository.save(new Reservation(member, schedule, theme));

        return new ReservationResponse(reservation);
    }

    private Schedule getScheduleOf(ReservationDate reservationDate, ReservationTime reservationTime) {
        return scheduleRepository.findByDateAndTime(reservationDate, reservationTime)
                    .orElseGet(() -> scheduleRepository.save(new Schedule(reservationDate, reservationTime)));
    }

    private ReservationTime findTimeById(long timeId) {
        return reservationTimeRepository.getById(timeId);
    }

    private Theme findThemeById(long themeId) {
        return themeRepository.getById(themeId);
    }

    private Member findMemberById(long memberId) {
        return memberRepository.getById(memberId);
    }

    private void validate(ReservationDate reservationDate, ReservationTime reservationTime, Theme theme) {
        validateIfBefore(reservationDate, reservationTime);
        validateDuplicated(reservationDate, reservationTime, theme);
    }

    private void validateIfBefore(ReservationDate date, ReservationTime time) {
        LocalDateTime value = LocalDateTime.of(date.getValue(), time.getStartAt());
        if (value.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException("현재보다 이전으로 일정을 설정할 수 없습니다.");
        }
    }

    private void validateDuplicated(ReservationDate date, ReservationTime reservationTime, Theme theme) {
        if (reservationRepository.existsByScheduleDateAndScheduleTimeIdAndThemeId(date, reservationTime.getId(),
                theme.getId())) {
            throw new InvalidReservationException("선택하신 테마와 일정은 이미 예약이 존재합니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream().map(ReservationResponse::new).toList();
    }

    public void deleteById(long id) {
        reservationRepository.deleteById(id);
    }

    public void deleteById(long reservationId, long memberId) {
        validateAuthority(reservationId, memberId);
        reservationRepository.deleteById(reservationId);
    }

    private void validateAuthority(long reservationId, long memberId) {
        if (!reservationRepository.existsById(reservationId)) {
            return;
        }
        if (reservationRepository.getById(reservationId).getMember().getId() != memberId) {
            throw new UnauthorizedException("예약을 삭제할 권한이 없습니다.");
        }
    }

    public List<ReservationResponse> findByCondition(ReservationFindRequest reservationFindRequest) {
        ReservationDate dateFrom = ReservationDate.of(reservationFindRequest.dateFrom());
        ReservationDate dateTo = ReservationDate.of(reservationFindRequest.dateTo());
        return reservationRepository.findBy(reservationFindRequest.memberId(), reservationFindRequest.themeId(),
                dateFrom, dateTo).stream().map(ReservationResponse::new).toList();
    }
}
