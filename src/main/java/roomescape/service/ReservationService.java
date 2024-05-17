package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.LoginMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.ReservationDetailResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse save(LoginMember loginMember,
                                    ReservationRequest reservationRequest) {

        ReservationTime requestedTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme requestedTheme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member requestedMember = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));
        Reservation beforeSaveReservation = reservationRequest.toReservation(requestedMember, requestedTime,
                requestedTheme);

        Reservations reservations = new Reservations(reservationRepository.findAll());
        if (reservations.hasSameReservation(beforeSaveReservation)) {
            throw new RoomescapeException(DUPLICATE_RESERVATION);
        }
        if (beforeSaveReservation.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }

        return ReservationResponse.from(reservationRepository.save(beforeSaveReservation));
    }

    public ReservationResponse saveByAdmin(AdminReservationRequest reservationRequest) {
        ReservationTime requestedTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme requestedTheme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member requestedMember = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));

        Reservation beforeSaveReservation = new Reservation(reservationRequest.date(), requestedTime, requestedTheme,
                requestedMember);

        Reservations reservations = new Reservations(reservationRepository.findAll());
        if (reservations.hasSameReservation(beforeSaveReservation)) {
            throw new RoomescapeException(DUPLICATE_RESERVATION);
        }
        if (beforeSaveReservation.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }

        return ReservationResponse.from(reservationRepository.save(beforeSaveReservation));
    }



    public List<ReservationResponse> findAll() {
        Reservations reservations = new Reservations(reservationRepository.findAll());
        return reservations.getReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> searchReservation(Long themeId, Long memberId, LocalDate dateFrom,
                                                       LocalDate dateTo) {
        Reservations reservations = new Reservations(reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo));
        return reservations.getReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void delete(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationDetailResponse> findAllByMemberId(long userId) {
        return reservationRepository.findAllByMemberId(userId).stream()
                .map(ReservationDetailResponse::from)
                .toList();
    }
}
