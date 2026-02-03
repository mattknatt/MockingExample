package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@link BookingSystem} class.
 * This class tests the booking logic, availability checks, and cancellation process.
 */
@DisplayName("Booking System Tests")
@ExtendWith(MockitoExtension.class)
class BookingSystemTest {

    private static final String ROOM_ID = "room_id";
    private static final String BOOKING_ID = "booking_id";
    private static final LocalDateTime CURRENT_TIME = LocalDateTime.of(2026, 1, 28, 12, 0);
    private static final LocalDateTime START_TIME = CURRENT_TIME.plusHours(1);
    private static final LocalDateTime END_TIME = CURRENT_TIME.plusHours(3);
    @Mock
    TimeProvider timeProvider;
    @Mock
    RoomRepository roomRepository;
    @Mock
    NotificationService notificationService;
    @Mock
    Room room;

    @InjectMocks
    BookingSystem bookingSystem;

    private static Stream<Arguments> bookRoom_nullParameters() {
        return Stream.of(
                Arguments.of(null, START_TIME, END_TIME),
                Arguments.of(ROOM_ID, null, null),
                Arguments.of(ROOM_ID, START_TIME, null),
                Arguments.of(ROOM_ID, null, END_TIME),
                Arguments.of(null, null, null),
                Arguments.of(null, START_TIME, null),
                Arguments.of(null, null, END_TIME));

    }

    public static Stream<Arguments> getAvailableRooms_nullParameters() {
        return Stream.of(
                Arguments.of(START_TIME, null),
                Arguments.of(null, END_TIME),
                Arguments.of(null, null));
    }

    @BeforeEach
    void setUp() {

    }

    /**
     * Verifies that booking fails when any of the required parameters (roomID, startTime, endTime) are null.
     */
    @DisplayName("Book room: Null parameters should throw IllegalArgumentException")
    @ParameterizedTest
    @MethodSource("bookRoom_nullParameters")
    void nullParametersShouldThrowException(String roomId, LocalDateTime startTime, LocalDateTime endTime) {

        assertThatThrownBy(() -> bookingSystem.bookRoom(roomId, startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bokning kräver giltiga start- och sluttider samt rum-id");

    }

    /**
     * Verifies that booking fails if the requested start time is in the past.
     */
    @DisplayName("Book room: Start time in the past should throw IllegalArgumentException")
    @Test
    void bookRoom_shouldThrowExceptionIfStartTime_isBeforeCurrentTime() {

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, CURRENT_TIME.minusHours(1), END_TIME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kan inte boka tid i dåtid");

    }

    /**
     * Verifies that booking fails if the end time is before the start time.
     */
    @DisplayName("Book room: End time before start time should throw IllegalArgumentException")
    @Test
    void bookRoom_shouldThrowExceptionIfEndTime_isBeforeStartTime() {

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, CURRENT_TIME, CURRENT_TIME.minusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sluttid måste vara efter starttid");

    }

    /**
     * Verifies that booking fails if the room does not exist in the repository.
     */
    @DisplayName("Book room: Non-existent room should throw IllegalArgumentException")
    @Test
    void bookRoom_shouldThrowExceptionIfRoom_doesNotExist() {

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        Mockito.when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingSystem.bookRoom(ROOM_ID, START_TIME, END_TIME)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rummet existerar inte");
    }

    /**
     * Verifies that booking returns false if the room is already booked for the requested period.
     */
    @DisplayName("Book room: Should return false if room is not available")
    @Test
    void bookRoom_returnsFalseIfRoomIsNotAvailable() {
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        Mockito.when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        Mockito.when(room.isAvailable(START_TIME, END_TIME)).thenReturn(false);

        assertThat(bookingSystem.bookRoom(ROOM_ID, START_TIME, END_TIME)).isFalse();

    }

    /**
     * Verifies that a booking is successfully created and saved when the room is available.
     */
    @DisplayName("Book room: Should successfully add booking when room is available")
    @Test
    void bookRoom_addsBookingAndSavesRoom_whenAvailable() {
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
        Mockito.when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        Mockito.when(room.isAvailable(START_TIME, END_TIME)).thenReturn(true);

        boolean result = bookingSystem.bookRoom(ROOM_ID, START_TIME, END_TIME);

        assertThat(result).isTrue();
        Mockito.verify(room).addBooking(Mockito.any(Booking.class));
        Mockito.verify(roomRepository).save(room);
    }

    /**
     * Verifies that the booking is successful even if the notification service fails.
     * The system should be resilient to notification failures.
     */
    @DisplayName("Book room: Should return true even if notification fails")
    @Test
    void bookRoom_shouldReturnTrue_evenIfNotificationFails() throws NotificationException {
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
        Mockito.when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));
        Mockito.when(room.isAvailable(START_TIME, END_TIME)).thenReturn(true);
        Mockito.doThrow(new NotificationException("Notification failed")).when(notificationService).sendBookingConfirmation(Mockito.any(Booking.class));

        boolean result = bookingSystem.bookRoom(ROOM_ID, START_TIME, END_TIME);

        assertThat(result).isTrue();
        Mockito.verify(room).addBooking(Mockito.any(Booking.class));
        Mockito.verify(roomRepository).save(room);
        Mockito.verify(notificationService).sendBookingConfirmation(Mockito.any(Booking.class));

    }

    /**
     * Verifies that getAvailableRooms fails when start or end time is null.
     */
    @DisplayName("Get available rooms: Null parameters should throw IllegalArgumentException")
    @ParameterizedTest
    @MethodSource("getAvailableRooms_nullParameters")
    void getAvailableRooms_nullParametersThrowsException(LocalDateTime startTime, LocalDateTime endTime) {

        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(startTime, endTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Måste ange både start- och sluttid");
    }

    /**
     * Verifies that getAvailableRooms fails if the end time is before the start time.
     */
    @DisplayName("Get available rooms: End time before start time should throw IllegalArgumentException")
    @Test
    void getAvailableRooms_endTimeBeforeStartTime_ThrowsException() {

        assertThatThrownBy(() -> bookingSystem.getAvailableRooms(START_TIME, START_TIME.minusHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sluttid måste vara efter starttid");

    }

    /**
     * Verifies that getAvailableRooms returns only the rooms that are available for the specified time period.
     */
    @DisplayName("Get available rooms: Should return only available rooms")
    @Test
    void getAvailableRooms_returnsOnlyAvailableRooms() {
        Room availableRoom = Mockito.mock(Room.class);
        Room unavailableRoom = Mockito.mock(Room.class);

        Mockito.when(roomRepository.findAll()).thenReturn(List.of(availableRoom, unavailableRoom));
        Mockito.when(availableRoom.isAvailable(START_TIME, END_TIME)).thenReturn(true);
        Mockito.when(unavailableRoom.isAvailable(START_TIME, END_TIME)).thenReturn(false);

        List<Room> result = bookingSystem.getAvailableRooms(START_TIME, END_TIME);

        assertThat(result).containsExactly(availableRoom);
    }

    /**
     * Verifies that cancelling a booking fails if the booking ID is null.
     */
    @DisplayName("Cancel booking: Null booking ID should throw IllegalArgumentException")
    @Test
    void cancelBooking_throwsException_ifBookingIdIsNull() {

        assertThatThrownBy(() -> bookingSystem.cancelBooking(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Boknings-id kan inte vara null");

    }

    /**
     * Verifies that cancelBooking returns false if no room is found that contains the specified booking ID.
     */
    @DisplayName("Cancel booking: Should return false if booking ID is not found")
    @Test
    void cancelBooking_returnsFalse_ifNoRoomWithBooking() {

        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));
        Mockito.when(room.hasBooking(BOOKING_ID)).thenReturn(false);

        boolean result = bookingSystem.cancelBooking(BOOKING_ID);

        assertThat(result).isFalse();

    }

    /**
     * Verifies that cancelling a booking fails if the booking has already started or finished.
     */
    @DisplayName("Cancel booking: Cannot cancel booking that has already started")
    @Test
    void cancelBooking_throwsException_ifStartTime_isBeforeCurrentTime() {

        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);

        Booking booking = new Booking(BOOKING_ID, ROOM_ID, CURRENT_TIME.minusHours(1), END_TIME);
        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));
        Mockito.when(room.hasBooking(BOOKING_ID)).thenReturn(true);
        Mockito.when(room.getBooking(BOOKING_ID)).thenReturn(booking);

        assertThatThrownBy(() -> bookingSystem.cancelBooking(BOOKING_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kan inte avboka påbörjad eller avslutad bokning");
    }

    /**
     * Verifies that a booking is successfully removed from the room when canceled.
     */
    @DisplayName("Cancel booking: Should successfully remove booking")
    @Test
    void cancelBooking_removesBooking() {

        Booking booking = new Booking(BOOKING_ID, ROOM_ID, START_TIME, END_TIME);
        Mockito.when(timeProvider.getCurrentTime()).thenReturn(CURRENT_TIME);
        Mockito.when(roomRepository.findAll()).thenReturn(List.of(room));
        Mockito.when(room.hasBooking(BOOKING_ID)).thenReturn(true);
        Mockito.when(room.getBooking(BOOKING_ID)).thenReturn(booking);

        bookingSystem.cancelBooking(BOOKING_ID);
        Mockito.verify(room).removeBooking(BOOKING_ID);

    }
}

