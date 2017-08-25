package com.example.demo;

import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private CloudinaryConfig cloudc;

    @Autowired
    private MessageRepository messageRepository;

    @RequestMapping("/")
    public String home(Model model){
        model.addAttribute("message", new Message());

        return "add";
    }

    @RequestMapping(value="/register", method = RequestMethod.GET)
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new User());
        return "/register";
    }

    @RequestMapping(value="/register", method = RequestMethod.POST)
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {

        model.addAttribute("user", user);

        if (result.hasErrors()) {
            return "register";
        } else {
            model.addAttribute("message", "User Account Successfully Created");
        }
        return "redirect:/";
    }
    @RequestMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String newPost(Model model, Principal principal) {
        model.addAttribute("add", new Message());
         return "add";
    }

    @RequestMapping(value = "/loginSuccess", method = RequestMethod.GET)
    public String loginSuccess(Model model){
        Iterable<Message> mesList = messageRepository.findAll();
        model.addAttribute("mesList", mesList);
        return "/list";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String housePOST(@RequestParam("file") MultipartFile file, Message message, RedirectAttributes redirectAttributes,
                            Model model,  Principal principal, BindingResult result){

        if (file.isEmpty()){
            redirectAttributes.addFlashAttribute("message","Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {
            Map uploadResult =  cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));

            model.addAttribute("message","You successfully uploaded '" + file.getOriginalFilename() + "'");
            String filename = uploadResult.get("public_id").toString() + "." + uploadResult.get("format").toString();
            //String effect = p.getTitle();
            message.setPhoto("<img src='http://res.cloudinary.com/henokzewdie/image/upload/" +filename+"' width='100px'/>");
            //  house.setDetailphoto("<img src='http://res.cloudinary.com/henokzewdie/image/upload/" +filename+"' width='500px'/>");

            //System.out.printf("%s\n", cloudc.createUrl(filename,900,900, "fit"));

        } catch (IOException e){
            e.printStackTrace();
            model.addAttribute("message", "Sorry I can't upload that!");
        }

        message.setPostDate(new Date());
        message.setPostUser(principal.getName());
        messageRepository.save(message);

        return "redirect:/loginSuccess";
    }


}