package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getAll();

    Optional<User> getById(long userId);

    Optional<User> getByEmail(String userEmail);

    User create(User user);

    void update(User user);

    void delete(User user);
}
