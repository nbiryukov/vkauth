package org.vkauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.vkauth.User;
import org.vkauth.UserService;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class MainController {

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

            // Авторизируем пользователя
            UserService service = new UserService();
            User user = service.authorization(code);
            model.put("firstNameUser", user.getFirstName());
            model.put("lastNameUser", user.getLastName());
            model.put("friends", user.getFriends());

            session.setAttribute("user", user);
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
                "?client_id=" + UserService.APP_ID +
                "&display=popup" +
                "&redirect_uri=" + UserService.REDIRECT_URI +
                "&scope=friends" +
                "&response_type=code" +
                "&v=5.85"));
    }
}
