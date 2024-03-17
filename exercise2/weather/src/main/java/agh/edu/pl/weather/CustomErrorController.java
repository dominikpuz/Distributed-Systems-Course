package agh.edu.pl.weather;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Error error = new Error(Integer.parseInt(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE).toString()),
                request.getAttribute(RequestDispatcher.ERROR_MESSAGE).toString());
        setUpErrorPage(model, error);
        return "error";
    }

    @GetMapping(path = "/error400")
    public String handle400Error(Model model) {
        Error error = new Error(400, "Bad request");
        setUpErrorPage(model, error);
        return "error";
    }

    @GetMapping(path = "/error404")
    public String handle404Error(Model model) {
        Error error = new Error(404, "Resource not found");
        setUpErrorPage(model, error);
        return "error";
    }

    private void setUpErrorPage(Model model, Error error) {
        Map<String, Error> map = new HashMap<>();
        map.put("error", error);
        model.addAllAttributes(map);
    }
}
