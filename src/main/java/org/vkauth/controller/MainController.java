package org.vkauth.controller;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.OAuthException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.apps.responses.GetResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    private final int APP_ID = 6702883;
    private final String CLIENT_SECRET = "MwjNmsj3kmGOZYWWLfnv";
    private final String REDIRECT_URI = "https://authvk.herokuapp.com/lk";

    @GetMapping("/")
    public String main() {
        return "index";
    }

    @GetMapping("/lk")
    public String authVk(@RequestParam(name = "code") String code, Map<String, Object> model) {

        // Инициализация
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);

        // Авторизация
        UserAuthResponse authResponse = null;
        try {
            authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI, code)
                    .execute();
        } catch (OAuthException e) {
            e.getRedirectUri();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());

        // Информация о пользователе
        try {
            List<UserXtrCounters> users = (List<UserXtrCounters>) vk.users().get(actor).execute();
            for (UserXtrCounters user : users) {
                model.put("firstName", user.getFirstName());
                model.put("lastName", user.getLastName());
            }
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return "lk";
    }

    @PostMapping(value = "/auth")
    public ModelAndView openAuth() {

        return new ModelAndView(new RedirectView("https://oauth.vk.com/authorize" +
                "?client_id=6702883" +
                "&display=page" +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=friends" +
                "&response_type=code" +
                "&v=5.85"));
    }
}
