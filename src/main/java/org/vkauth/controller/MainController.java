package org.vkauth.controller;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.OAuthException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.friends.UserXtrLists;
import com.vk.api.sdk.objects.friends.responses.GetFieldsResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.friends.FriendsGetOrder;
import com.vk.api.sdk.queries.users.UserField;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.vkauth.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes(types = User.class)
public class MainController {

    private final int APP_ID = 6702883;
    private final String CLIENT_SECRET = "MwjNmsj3kmGOZYWWLfnv";
    private final String REDIRECT_URI = "https://authvk.herokuapp.com/lk";

    @GetMapping("/")
    public String main(HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "redirect:/profile";
        }

        return "index";
    }

    @GetMapping("/lk")
    public String authVk(@RequestParam(name = "code") String code,
                         Map<String, Object> model,
                         HttpSession session) {

        if (session.getAttribute("user") == null) {

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

            User user = null;
            try {
                // Получаем информацию о пользователе и добавляем ее в модель
                List<UserXtrCounters> users = vk.users().get(actor).execute();
                model.put("firstNameUser", users.get(0).getFirstName());
                model.put("lastNameUser", users.get(0).getLastName());
                user = new User(users.get(0).getFirstName(), users.get(0).getLastName());


                // Получаем друзей и добавляем их в модель
                GetFieldsResponse response = vk.friends()
                        .get(actor, UserField.SEX)
                        .order(FriendsGetOrder.HINTS)
                        .count(5)
                        .execute();
                List<UserXtrLists> friendsResponse = response.getItems();
                List<User> friends = new ArrayList<>();
                for (UserXtrLists friend : friendsResponse) {
                    friends.add(new User(friend.getFirstName(), friend.getLastName()));
                }
                model.put("friends", friends);
                user.setFriends(friends);

                session.setAttribute("user", user);
            } catch (ApiException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String profile(Map<String, Object> model, HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user != null) {
            model.put("firstNameUser", user.getFirstName());
            model.put("lastNameUser", user.getLastName());
            model.put("friends", user.getFriends());

            return "profile";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/auth")
    public ModelAndView openAuth() {

        return new ModelAndView(new RedirectView("https://oauth.vk.com/authorize" +
                "?client_id=" + APP_ID +
                "&display=popup" +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=friends" +
                "&response_type=code" +
                "&v=5.85"));
    }
}
