package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.DomainValidationException;
import roomescape.global.exception.IllegalRequestException;
import roomescape.global.exception.NoSuchRecordException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.dto.MemberReservation;
import roomescape.reservation.dto.MemberReservationAddRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@Service
public class ReservationService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(MemberRepository memberRepository,
                              ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findAllReservation() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllByMemberAndThemeAndPeriod(Long memberId, Long themeId, LocalDate dateFrom,
                                                                      LocalDate dateTo) {
        return reservationRepository.findByMemberAndThemeAndPeriod(memberId, themeId,
                        dateFrom, dateTo).stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<MemberReservation> findAllByMemberWithStatus(Long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(reservation -> new MemberReservation(reservation, calculateReservationWaitingNumber(reservation)))
                .toList();
    }

    public int calculateReservationWaitingNumber(Reservation reservation) {
        List<Reservation> reservations = reservationRepository.findByDateAndTimeAndTheme(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getThemeId()
        );

        return (int) reservations.stream()
                .filter(other -> other.isWaitingOrderHigherThan(reservation))
                .count() + 1;
    }

    public List<WaitingResponse> findWaitings() {
        return reservationRepository.findWaitings().stream()
                .map(reservation -> new WaitingResponse(reservation, calculateReservationWaitingNumber(reservation)))
                .toList();
    }
    
    public ReservationResponse saveMemberReservation(Long memberId, MemberReservationAddRequest request) {
        List<Reservation> earlierReservations = reservationRepository.findByDateAndTimeAndTheme(
                request.date(),
                request.timeId(),
                request.themeId()
        );

        Reservation reservation = new Reservation(
                null,
                getMember(memberId),
                request.date(),
                getReservationTime(request.timeId()),
                getTheme(request.themeId())
        );

        if (reservation.isPast()) {
            throw new DomainValidationException(reservation.getDate() + ": 예약 날짜는 현재 보다 이전일 수 없습니다");
        }
        if (earlierReservations.stream().anyMatch(earlierReservation -> earlierReservation.isReservedBy(memberId))) {
            throw new IllegalRequestException("해당 아이디로 진행되고 있는 예약(대기)이 이미 존재합니다");
        }

        Reservation saved = reservationRepository.save(reservation);
        return new ReservationResponse(saved);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchRecordException("ID: " + memberId + " 해당하는 회원을 찾을 수 없습니다"));
    }

    private ReservationTime getReservationTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchRecordException("해당하는 예약시간이 존재하지 않습니다 ID: " + timeId));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchRecordException("해당하는 테마가 존재하지 않습니다 ID: " + themeId));
    }

    public void removeReservation(long id) {
        reservationRepository.deleteById(id);
    }
}
