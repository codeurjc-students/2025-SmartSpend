package com.smartspend.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards non-file browser routes to the Angular entry point so SPA refresh works.
 */
@Controller
public class AngularRoutingController {

    /**
     * Captures first and second level routes without file extensions and forwards them to index.html.
     *
     * @return the forward view name to Angular's entry page.
     */
    @RequestMapping({"/{path:[^\\.]*}", "/{path:[^\\.]*}/{subpath:[^\\.]*}"})
    public String forwardToAngularIndex() {
        return "forward:/index.html";
    }
}
