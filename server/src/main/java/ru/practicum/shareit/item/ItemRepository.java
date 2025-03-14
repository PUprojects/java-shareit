package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.dto.RequestAnswerDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item i join fetch i.owner as ow where ow.id=:ownerId")
    List<Item> findByOwner(@Param("ownerId") long ownerId);

    Optional<Item> findById(long itemId);

    @Query("select i from Item i where (upper(i.name) like upper(concat('%', :text, '%')) " +
            "or upper(i.description) like upper(concat('%', :text, '%'))) " +
            "and i.available = :isAvailable")
    List<Item> search(@Param("text") String text, @Param("isAvailable") boolean isAvailable);

    List<RequestAnswerDto> findByRequest_id(long requestId);
}
