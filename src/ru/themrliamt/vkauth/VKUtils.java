package ru.themrliamt.vkauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//Говнокод творит чудеса))
public class VKUtils {

    private String VK_TOKEN;
    private String SEND_MESSAGE = "https://api.vk.com/method/messages.send?v=5.52&access_token=%s&message=%s&user_id=%s";

    public VKUtils(String VK_TOKEN) {
        this.VK_TOKEN = VK_TOKEN;
    }

    public void sendCode(String code, String user) {
        String u = String.format(SEND_MESSAGE, VK_TOKEN, code, user);

        try {
            URL obj = new URL(u);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");

            connection.getInputStream().close();
        } catch (IOException ignored) {}


    }

}
