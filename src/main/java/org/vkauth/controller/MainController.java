package org.vkauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Controller
public class MainController {

    @GetMapping
    public String main() {
        return "index";
    }

    @PostMapping(value = "/auth")
    public ModelAndView openAuth() {
        
        return new ModelAndView(new RedirectView("https://oauth.vk.com/authorize" +
                "?client_id=6702883&display=popup" +
                "&redirect_uri=https://authvk.herokuapp.com/lk" +
                "&scope=friends" +
                "&response_type=code" +
                "&v=5.85"));
    }
}
