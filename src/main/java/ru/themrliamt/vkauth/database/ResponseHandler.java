package ru.themrliamt.vkauth.database;

public interface ResponseHandler<H, R> {
    R handleResponse(H handle) throws Exception;
}
