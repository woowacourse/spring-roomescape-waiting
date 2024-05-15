package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.IllegalReservationDateTimeRequestException;
import roomescape.exception.SaveDuplicateContentException;
import roomescape.member.dao.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.theme.dao.ThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.dao.TimeRepository;
import roomescape.time.domain.Time;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationDao reservationDao, TimeRepository timeRepository, ThemeRepository themeRepository,
            MemberRepository memberRepository) {
        this.reservationDao = reservationDao;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse addReservation(ReservationRequest reservationRequest,
            MemberProfileInfo memberProfileInfo) {
        Time time = timeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new IllegalReservationDateTimeRequestException("해당 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new BadRequestException("선택하신 테마가 존재하지 않습니다."));
        validateReservationRequest(reservationRequest, time);
        Reservation reservation = reservationRequest.toReservation(time, theme);
        Reservation savedReservation = reservationDao.save(reservation);
        reservationDao.saveMemberReservation(savedReservation.getId(), memberProfileInfo.id());
        return ReservationResponse.fromReservation(savedReservation);
    }

    public ReservationResponse addReservation(AdminReservationRequest reservationRequest) {
        Time time = timeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new IllegalReservationDateTimeRequestException("해당 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new BadRequestException("선택한 테마가 존재하지 않습니다."));
        Member member = memberRepository.findById(reservationRequest.memberId()).orElseThrow();
        Reservation reservation = new Reservation(member.getName(), reservationRequest.date(), time, theme);
        Reservation savedReservation = reservationDao.save(reservation);
        reservationDao.saveMemberReservation(savedReservation.getId(), member.getId());
        return ReservationResponse.fromReservation(savedReservation);
    }

    private void validateReservationRequest(ReservationRequest reservationRequest, Time time) {
        if (reservationRequest.date()
                .isBefore(LocalDate.now())) {
            throw new IllegalReservationDateTimeRequestException("지난 날짜의 예약을 시도하였습니다.");
        }
        validateDuplicateReservation(reservationRequest, time);
    }

    private void validateDuplicateReservation(ReservationRequest reservationRequest, Time time) {
        List<Time> bookedTimes = getBookedTimesOfThemeAtDate(reservationRequest.themeId(), reservationRequest.date());
        if (isTimeBooked(time, bookedTimes)) {
            throw new SaveDuplicateContentException("해당 시간에 예약이 존재합니다.");
        }
    }

    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationDao.findAllOrderByDateAndTime();

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(long themeId, LocalDate date) {
        List<Time> allTimes = timeRepository.findAllByOrderByStartAtAsc();
        List<Time> bookedTimes = getBookedTimesOfThemeAtDate(themeId, date);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.fromTime(time, isTimeBooked(time, bookedTimes)))
                .toList();
    }

    public List<ReservationResponse> findReservationsByConditions(ReservationConditionSearchRequest request) {
        List<Long> reservationIds = reservationDao.findReservationIdsByMemberId(request.memberId());
        List<Reservation> reservations = reservationIds.stream()
                .map(reservationDao::findByIdOrderByDate)
                .toList();
        return reservations.stream()
                .filter(reservation -> reservation.isReservedAtPeriod(request.dateFrom(), request.dateTo()))
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    private List<Time> getBookedTimesOfThemeAtDate(long themeId, LocalDate date) {
        List<Reservation> reservationsOfThemeInDate = reservationDao.findAllByThemeIdAndDate(themeId, date);
        return extractReservationTimes(reservationsOfThemeInDate);
    }

    private List<Time> extractReservationTimes(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
    }

    private boolean isTimeBooked(Time time, List<Time> bookedTimes) {
        return bookedTimes.contains(time);
    }

    public void removeReservations(long reservationId) {
        reservationDao.deleteById(reservationId);
    }

}
