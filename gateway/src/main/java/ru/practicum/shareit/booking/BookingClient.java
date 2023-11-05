package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingAddDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addBooking(Long userId, BookingAddDto bookingAddDto) {
        return post("", userId, bookingAddDto);
    }

    public ResponseEntity<Object> updateBookingStatus(Long userId, Boolean approved, Long bookingId) {
        String path = "/" + bookingId + "?approved=" + approved;
        return patch(path, userId, null, null);
    }

    public ResponseEntity<Object> getBookingById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllUserBookings(BookingStatus status, Long userId, int from, int size) {
        String path = "?state=" + status.name() + "&from=" + from + "&size=" + size;
        return get(path, userId, null);
    }

    public ResponseEntity<Object> getAllItemBookingsUser(Long userId, BookingStatus status, int from, int size) {
        String path = "/owner?state=" + status.name() + "&from=" + from + "&size=" + size;
        return get(path, userId, null);
    }
}