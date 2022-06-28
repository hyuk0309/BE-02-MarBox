package prgrms.marco.be02marbox.domain.theater.service;

import static java.util.stream.Collectors.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import prgrms.marco.be02marbox.domain.exception.custom.BadRequestTheaterException;
import prgrms.marco.be02marbox.domain.theater.Seat;
import prgrms.marco.be02marbox.domain.theater.Theater;
import prgrms.marco.be02marbox.domain.theater.TheaterRoom;
import prgrms.marco.be02marbox.domain.theater.dto.RequestCreateTheaterRoom;
import prgrms.marco.be02marbox.domain.theater.dto.ResponseFindTheater;
import prgrms.marco.be02marbox.domain.theater.dto.ResponseFindTheaterRoom;
import prgrms.marco.be02marbox.domain.theater.repository.TheaterRepository;
import prgrms.marco.be02marbox.domain.theater.repository.TheaterRoomRepository;
import prgrms.marco.be02marbox.domain.theater.service.utils.SeatConverter;
import prgrms.marco.be02marbox.domain.theater.service.utils.TheaterConverter;
import prgrms.marco.be02marbox.domain.theater.service.utils.TheaterRoomConverter;

@Service
@Transactional(readOnly = true)
public class TheaterRoomService {

	private static final String WRONG_THEATER_ID_ERR_MSG = "올바르지 않은 극장 ID 값입니다.";

	private final TheaterRoomRepository theaterRoomRepository;
	private final TheaterRepository theaterRepository;
	private final SeatConverter seatConverter;
	private final TheaterRoomConverter theaterRoomConverter;
	private final TheaterConverter theaterConverter;

	public TheaterRoomService(TheaterRoomRepository theaterRoomRepository,
		TheaterRepository theaterRepository,
		SeatConverter seatConverter,
		TheaterRoomConverter theaterRoomConverter,
		TheaterConverter theaterConverter) {
		this.theaterRoomRepository = theaterRoomRepository;
		this.theaterRepository = theaterRepository;
		this.seatConverter = seatConverter;
		this.theaterRoomConverter = theaterRoomConverter;
		this.theaterConverter = theaterConverter;
	}

	/**
	 * 새로운 상영관 추가
	 * @param requestCreateTheaterRoom 극장, 상영관 이름, 좌석 정보
	 * @return 생성된 id
	 * @throws  BadRequestTheaterException 극장 정보가 존재하지 않는 경우
	 */
	@Transactional
	public Long save(RequestCreateTheaterRoom requestCreateTheaterRoom) {
		Theater theater = theaterRepository.findById(requestCreateTheaterRoom.theaterId())
			.orElseThrow(() -> new BadRequestTheaterException(WRONG_THEATER_ID_ERR_MSG));

		TheaterRoom newTheaterRoom = new TheaterRoom(theater, requestCreateTheaterRoom.name());
		TheaterRoom savedTheaterRoom = theaterRoomRepository.save(newTheaterRoom);
		List<Seat> seatList = requestCreateTheaterRoom.requestCreateSeats().stream().map(requestCreateSeat ->
			seatConverter.convertFromRequestSeatToSeat(savedTheaterRoom, requestCreateSeat)
		).collect(toList());
		savedTheaterRoom.addSeats(seatList);
		return savedTheaterRoom.getId();
	}

	/**
	 * 등록된 모든 상영관 조회
	 * @return 상영관 리스트
	 */
	public List<ResponseFindTheaterRoom> findAll() {
		return theaterRoomRepository.findAll().stream()
			.map(theaterRoom -> {
				ResponseFindTheater responseFindTheater = theaterConverter.convertFromTheaterToResponseFindTheater(
					theaterRoom.getTheater());
				return theaterRoomConverter.convertFromTheaterRoomToTheaterResponseFindTheaterRoom(responseFindTheater,
					theaterRoom);
			})
			.collect(toList());
	}
}