package org.vkauth.controller;

import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.vkauth.User;
import org.vkauth.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @GetMapping("/")
    public String main(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return "redirect:/profile";
        }

        return "index";
    }

    @GetMapping("/lk")
    public String authVk(@RequestParam(name = "code") String code,
                         Map<String, Object> model,
                         HttpServletRequest request,
                         HttpServletResponse responce) {

        if (request.getCookies() == null) {

            // Авторизируем пользователя
            UserService service = new UserService();
            User user = service.authorization(code);
            model.put("firstNameUser", user.getFirstName());
            model.put("lastNameUser", user.getLastName());
            model.put("friends", user.getFriends());

            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            Cookie cookie = new Cookie("user", userJson);
            cookie.setMaxAge(3600);
            responce.addCookie(cookie);
        }

        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String profile(Map<String, Object> model, HttpServletRequest request) {

        if (request.getCookies() != null) {
            String userJson = request.getCookies()[0].getValue();
            Gson gson = new Gson();
            User user = gson.fromJson(userJson, User.class);
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
                "?client_id=" + UserService.APP_ID +
                "&display=popup" +
                "&redirect_uri=" + UserService.REDIRECT_URI +
                "&scope=friends" +
                "&response_type=code" +
                "&v=5.85"));
    }
}
