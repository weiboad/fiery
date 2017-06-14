package org.weiboad.ragnar.server.controller.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class FieryErrorPage implements ErrorController {

    private static final String PATH = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @Value("${spring.profiles.active}")
    private String active;

    Logger log = LoggerFactory.getLogger(FieryErrorPage.class);

    @RequestMapping(value = PATH)
    public String error(Model model, HttpServletRequest request, HttpServletResponse response) {
        //return new ErrorJson(response.getStatus(), getErrorAttributes(request, debug));
        Map<String, Object> mapobj = getErrorAttributes(request, isDebug());

        model.addAttribute("status", response.getStatus());
        model.addAttribute("error", (String) mapobj.get("error"));
        model.addAttribute("trace", (String) mapobj.get("trace"));
        model.addAttribute("msg", (String) getErrorAttributes(request, true).get("message"));

        return "error";
    }

    public boolean isDebug() {
        //will display all 500
        return true;
        /*
        if (this.active.equalsIgnoreCase("dev")) {
            return true;
        }
        return false;*/
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

}
