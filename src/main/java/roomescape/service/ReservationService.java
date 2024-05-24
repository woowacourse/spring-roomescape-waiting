package roomescape.service;

import static roomescape.exception.ExceptionType.*;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.LoginMemberRequest;
import roomescape.dto.ReservationDetailResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.ExceptionType;
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
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse saveByUser(LoginMemberRequest loginMemberRequest,
                                          ReservationRequest reservationRequest) {
        ReservationTime requestedTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme requestedTheme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member requestedMember = memberRepository.findById(loginMemberRequest.id())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));

        return save(reservationRequest.toReservation(
                requestedMember, requestedTime, requestedTheme));
    }

    public ReservationResponse saveByAdmin(AdminReservationRequest reservationRequest) {
        ReservationTime requestedTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme requestedTheme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member requestedMember = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));

        return save(new Reservation(
                reservationRequest.date(), requestedTime, requestedTheme, requestedMember));
    }

    private ReservationResponse save(Reservation beforeSaveReservation) {
        if (beforeSaveReservation.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }
        Reservation savedReservation = reservationRepository.save(beforeSaveReservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> searchReservation(Long themeId, Long memberId, LocalDate dateFrom,
                                                       LocalDate dateTo) {
        return findReservationsBy(themeId, memberId, dateFrom, dateTo).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    private List<Reservation> findReservationsBy(Long themeId, Long memberId, LocalDate dateFrom,
                                                 LocalDate dateTo) {
        if (themeId != null && memberId != null) {
            return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo);
        }
        if (themeId != null) {
            return reservationRepository.findByThemeIdAndDateBetween(themeId, dateFrom, dateTo);
        }
        if (memberId != null) {
            return reservationRepository.findByMemberIdAndDateBetween(memberId, dateFrom, dateTo);
        }
        return reservationRepository.findByDateBetween(dateFrom, dateTo);
    }

    public void delete(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationDetailResponse> findAllByMemberId(long userId) {
        List<Reservation> reservationsByMember = reservationRepository.findAllByMemberId(userId);
        return reservationsByMember.stream()
                .map(reservation -> ReservationDetailResponse.from(
                        reservation,
                        reservationRepository.calculateIndexOf(reservation)))
                .toList();
    }

    @Transactional
    public void deleteWaiting(LoginMemberRequest loginMemberRequest, long id) {
        Reservation requestedReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION));

        if (requestedReservation.getMember().getId() != loginMemberRequest.id()) {
            throw new RoomescapeException(FORBIDDEN_DELETE);
        }
        if(isNotWaiting(requestedReservation)) {
            //todo 예외를 다른 예외로 구분하는 것이 좋을 것 같음
            throw new RoomescapeException(FORBIDDEN_DELETE);
        }

        reservationRepository.delete(requestedReservation);
    }

    private boolean isNotWaiting(Reservation requestedReservation) {
        long index = reservationRepository.calculateIndexOf(requestedReservation);
        //todo 상수 처리, 또는 다른 방법의 구현 고민
        return index == 1;
    }
}
