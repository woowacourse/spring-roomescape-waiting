package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.service.dto.request.ReservationConditionRequest;
import roomescape.service.dto.request.ReservationRequest;
import roomescape.service.dto.response.ReservationResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse createReservation(ReservationRequest reservationRequest, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_USER));

        ReservationTime reservationTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_RESERVATION_TIME));

        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new CustomException(ExceptionCode.NOT_FOUND_THEME));

        if (reservationRepository.existByTimeIdAndDate(reservationRequest.themeId(), reservationRequest.date())) {
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
        List<Reservation> reservations = reservationRepository.findAllReservations();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteReservation(Long id) {
        reservationRepository.delete(id);
    }

    public List<ReservationResponse> findAllReservationsByCondition(ReservationConditionRequest condition) {
        List<Reservation> reservations = reservationRepository.findAllReservationsByCondition(condition.themeId(), condition.memberId(), condition.dateFrom(), condition.dateTo());
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
