package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Получен запрос GET /users");
        List<UserDto> users = userService.getAll();
        log.info("Отправлен ответ GET /users. Всего {} пользователей : {}", users.size(), users);
        return users;
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable long id) {
        log.info("Получен запрос GET /users/{}", id);
        UserDto user = userService.getById(id);
        log.info("Отправлен ответ GET /users/{}: {}", id, user);
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid UserDto userDto) {
        log.info("Получен запрос POST /users: {}", userDto);
        UserDto newUser = userService.create(userDto);
        log.info("Отправлен ответ POST /users: {}", newUser);
        return newUser;
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable long id,
                          @RequestBody UserDto userDto) {
        log.info("Получен запрос PATCH /users/{}: {}", id, userDto);
        UserDto updateUser = userService.update(id, userDto);
        log.info("Отправлен ответ PATCH /users/{}: {}", id, updateUser);
        return updateUser;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable long id) {
        log.info("Получен запрос DELETE /users/{}", id);
        userService.delete(id);
        log.info("Запрос DELETE /users/{} обработан", id);
    }
}
