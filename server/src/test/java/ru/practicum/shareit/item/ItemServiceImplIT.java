package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.dto.ItemWithBookingAndCommentsDto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql("/sql/item_service/test_data.sql")
class ItemServiceImplIT {
    private final EntityManager manager;
    private final ItemServiceImpl itemService;

    @Test
    @DisplayName("Должен получать из базы данных информацию о вещи с бронированиями и комментариями")
    void shouldGetByIdWithBookingsAndComments() {
        ItemWithBookingAndCommentsDto item = itemService.getByIdWithBookingsAndComments(1L);

        assertThat(item, notNullValue());
        assertThat(item.id(), equalTo(1L));
        assertThat(item.name(), equalTo("First test item"));
        assertThat(item.description(), equalTo("First test item description"));
        assertThat(item.available(), equalTo(true));
        assertThat(item.requestId(), nullValue());
        assertThat(item.lastBooking(), notNullValue());
        //assertThat(item.lastBooking().);
        assertThat(item.nextBooking(), notNullValue());
        assertThat(item.comments(), notNullValue());
        assertThat(item.comments().size(), equalTo(2));
        assertThat(item.comments().get(0).text(), equalTo("First comment text"));
        assertThat(item.comments().get(1).text(), equalTo("Second comment text"));
    }
}