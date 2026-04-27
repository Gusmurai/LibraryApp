package ru.library.libraryapp.dao;

import ru.library.libraryapp.domains.Librarian;
import java.util.Optional;

/**
 * Интерфейс для работы с профилями сотрудников.
 */
public interface LibrarianDao {
    /**
     * Получение данных текущего авторизованного сотрудника.
     * @param dbLogin логин в СУБД
     * @return Optional с данными библиотекаря
     */
    Optional<Librarian> findByLogin(String dbLogin);
}