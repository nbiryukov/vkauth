package org.vkauth;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.friends.UserXtrLists;
import com.vk.api.sdk.objects.friends.responses.GetFieldsResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.friends.FriendsGetOrder;
import com.vk.api.sdk.queries.users.UserField;

import java.util.ArrayList;
import java.util.List;


public class UserService {

    public static final int APP_ID = 6702883;
    private final String CLIENT_SECRET = "MwjNmsj3kmGOZYWWLfnv";
    public static final String REDIRECT_URI = "https://authvk.herokuapp.com/lk";

    public User authorization(String code) {
        // Инициализация
        TransportClient transportClient = HttpTransportClient.getInstance();
        VkApiClient vk = new VkApiClient(transportClient);

        User user = null;
        // Авторизация
        UserAuthResponse authResponse = null;
        try {
            authResponse = vk.oauth()
                    .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI, code)
                    .execute();

            UserActor actor = new UserActor(authResponse.getUserId(), authResponse.getAccessToken());

            // Получаем информацию о пользователе
            List<UserXtrCounters> users = vk.users().get(actor).execute();
            user = new User(users.get(0).getFirstName(), users.get(0).getLastName());

            // Получаем друзей
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
            user.setFriends(friends);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return user;
    }
}
