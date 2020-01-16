package ru.themrliamt.vkauth;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class VKUtils {

    private String VK_TOKEN;
    private String SEND_MESSAGE = "https://api.vk.com/method/messages.send?v=5.52&access_token=%s&message=%s&user_id=%s";

    public VKUtils(String VK_TOKEN) {
        this.VK_TOKEN = VK_TOKEN;
    }

    public void sendCode(String code, String user) {

        try {
            String u = String.format(SEND_MESSAGE, VK_TOKEN, URLEncoder.encode("Ваш код для авторизации: " + code +
                    "\nВведите его в игре.", "UTF-8"), user);

            URL obj = new URL(u);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");

            connection.getInputStream().close();
        } catch (Exception ignored) {
        }


    }

}
