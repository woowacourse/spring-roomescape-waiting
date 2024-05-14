package roomescape.service;

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
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.ReservationConditionRequest;
import roomescape.service.dto.ReservationResponse;
import roomescape.service.dto.ReservationSaveRequest;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse saveReservation(ReservationSaveRequest reservationSaveRequest) {
        ReservationTime time = reservationTimeRepository.findById(reservationSaveRequest.timeId())
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 시간입니다."));

        Theme theme = themeRepository.findById(reservationSaveRequest.themeId())
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));

        Member member = memberRepository.findById(reservationSaveRequest.memberId())
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 회원입니다."));

        Reservation reservation = new Reservation(member, reservationSaveRequest.date(), time, theme);
        validateUnique(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    private void validateUnique(Reservation reservation) {
        boolean isReservationExist = reservationRepository.existByDateAndTimeAndTheme(reservation.getDate(),
                reservation.getTime(), reservation.getTheme());

        if (isReservationExist) {
            throw new RoomEscapeBusinessException("이미 존재하는 예약입니다.");
        }
    }

    public void deleteReservation(Long id) {
        Reservation foundReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약입니다."));

        reservationRepository.delete(foundReservation);
    }

    public List<ReservationResponse> findReservationsByCondition(
            ReservationConditionRequest reservationConditionRequest) {
        if (reservationConditionRequest.hasNoneCondition()) {
            List<Reservation> reservations = reservationRepository.findAll();
            return toReservationResponse(reservations);
        }

        Theme foundTheme = themeRepository.findById(reservationConditionRequest.themeId())
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));

        Member foundMember = memberRepository.findById(reservationConditionRequest.memberId())
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 회원입니다."));

        List<Reservation> reservations = reservationRepository.findByDateBetweenAndThemeAndMember(
                reservationConditionRequest.dateFrom(),
                reservationConditionRequest.dateTo(),
                foundTheme,
                foundMember
        );

        return toReservationResponse(reservations);
    }

    private List<ReservationResponse> toReservationResponse(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }
}
