package handler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/notice")
public class noticeController {
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public String register(ModelMap model) {
		return "notice";
	}
}
