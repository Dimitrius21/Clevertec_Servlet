package ru.clevertec.servlet.dao;


import java.util.List;
import java.util.Optional;

/**
 * интерфейс определяющий основные операции по доступу к хранилищу сущностей
 * @param <T> - определяет тип класса(сущности) с которой будут проводиться операции
 */
public interface SimpleDao<T> {

    public Optional<T> getById(long id);

    public List<T> getAll(int page, int quantity);

    public T create(T entity);

    public int update(T t);

    public void deleteById(long id);

}
