package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              TimeRepository timeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationRepository.findAllByOrderByDateAsc();

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<ReservationResponse> findReservationsByConditions(ReservationConditionSearchRequest request) {
        List<Reservation> reservations = reservationRepository.findAllByMember_Id(request.memberId());

        return reservations.stream()
                .filter(reservation -> reservation.isReservedAtPeriod(request.dateFrom(), request.dateTo()))
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<MyReservationResponse> findReservationByMemberId(Long id) {
        List<Reservation> reservationsByMember = reservationRepository.findAllByMember_IdOrderByDateAsc(id);
        return reservationsByMember.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public ReservationResponse addReservation(ReservationRequest reservationRequest) {
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new BadRequestException("선택하신 테마가 존재하지 않습니다."));
        Time time = timeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new BadRequestException("해당 예약 시간이 존재하지 않습니다."));
        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new BadRequestException("해당 멤버 정보가 존재하지 않습니다."));
        validateReservationRequest(reservationRequest, time);

        Reservation reservation = reservationRequest.toReservation(member, theme, time);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.fromReservation(savedReservation);
    }

    private void validateReservationRequest(ReservationRequest reservationRequest, Time time) {
        if (reservationRequest.isBeforeDate(LocalDate.now())) {
            throw new BadRequestException("지난 날짜의 예약을 시도하였습니다.");
        }
        List<Time> bookedTimes = getBookedTimesOfThemeAtDate(reservationRequest.themeId(), reservationRequest.date());
        if (bookedTimes.contains(time)) {
            throw new ConflictException("해당 시간에 예약이 존재합니다.");
        }
    }

    private List<Time> getBookedTimesOfThemeAtDate(long themeId, LocalDate date) {
        List<Reservation> reservations = reservationRepository.findAllByTheme_IdAndDate(themeId, date);
        return reservations.stream()
                .map(Reservation::getTime)
                .toList();
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(long themeId, LocalDate date) {
        List<Time> allTimes = timeRepository.findAllByOrderByStartAtAsc();
        List<Time> bookedTimes = getBookedTimesOfThemeAtDate(themeId, date);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.fromTime(time, bookedTimes.contains(time)))
                .toList();
    }

    public void removeReservations(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
