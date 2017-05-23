package org.weiboad.ragnar.server.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AboutUsPage {

    @RequestMapping(value = "/aboutus", method = RequestMethod.GET)

    public String aboutusPage(Model model) {
        return "aboutuspage";
    }
}
