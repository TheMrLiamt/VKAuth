package ru.themrliamt.vkauth.database;

//Говнокод творит чудеса))
public interface ResponseHandler<H, R> {
    R handleResponse(H handle) throws Exception;
}
