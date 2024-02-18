package com.cfp.controller;

import com.cfp.entity.User;
import com.cfp.repository.UserRepo;
import com.cfp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
//import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

//@Controller
@RestController
public class UserController {
    User currentuser = new User();
    @Autowired
    private UserRepo repo;
    @Autowired
    private UserService service;

    @GetMapping("/")
    public Object homepage(){

        return new ModelAndView("index");
    }

    @GetMapping("/updateProfile/{username}")
    public Object edit(@PathVariable String username, Model model) {
        // Retrieve the user by username
        User user = service.getUserByUsername(username);

        // Add the user object to the model
        model.addAttribute("user", user);

        // Return the view for editing the user profile
        return new ModelAndView("edit_user");
    }

    @Transactional
    @PostMapping("/updateuserDetails")
    public Object updateUserDetails(@ModelAttribute User updatedUser) {
        // Save the updated user details
        //User savedUser = service.save(updatedUser);
        repo.updateuser(updatedUser.getUsername(),updatedUser.getName(),updatedUser.getPhone(),updatedUser.getEmail());
        // Redirect to the profile page if the user details were successfully saved
        //if (savedUser != null) {
            // Redirect to the profile page
        Optional<User> userData = Optional.ofNullable(repo.findByUsername(updatedUser.getUsername()));
        userData.ifPresent(user -> currentuser = user);
            return new ModelAndView("redirect:/profile");
        //} else {
            // If something went wrong, redirect to the edit profile page again with an error message
            //return new ModelAndView("redirect:/edit_user/" + updatedUser.getUsername() + "?error=true");
       // }
    }

    @GetMapping("/profile")
    public Object getprofile(Model model){
        model.addAttribute("user", currentuser);
        return new ModelAndView("profile");
    }
    @GetMapping("/signup")
    public Object register(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return new ModelAndView("register");
    }

    @PostMapping("/registerUser")
    public ModelAndView registerAuthor(@ModelAttribute("user") User user, HttpSession session) {
        // Check if the username already exists
        User existingUser = repo.findByUsername(user.getUsername());
        User existingEmail = repo.findByEmail(user.getEmail());
        if (existingUser != null) {
            // Username already exists, set message and return to registration page
            session.setAttribute("message1", "Username already exists");
            return new ModelAndView("redirect:/signup");
        } else {
            // Username does not exist, proceed with registration
            service.registerUser(user);
            session.setAttribute("message1", "Registration successful");
            return new ModelAndView("redirect:/signup");
        }
    }


    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login", "user", new User());
    }

    @PostMapping("/signing")
    public ModelAndView loginAuthor(@ModelAttribute("user") @NotNull User user, Model model,HttpSession session) {
        String userID = user.getUsername();
        Optional<User> userData = Optional.ofNullable(repo.findByUsername(userID));

        if (userData.isPresent() && user.getPassword().equals(userData.get().getPassword())) {
            // If user exists and password matches, add user details to the model
            model.addAttribute("user", userData.get());
            currentuser=userData.get();
            return new ModelAndView("speaker_Dashboard");
        } else {
            // If user not found, set a message in session and redirect to login page
            session.setAttribute("message2", "User not found");
            return new ModelAndView("redirect:/login");
        }
    }

    @GetMapping("/dashboard")
    public Object dashboard(){
        return new ModelAndView("speaker_Dashboard");
    }

    @GetMapping("/logout")
    public Object logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext(); // Clear security context
        request.getSession().invalidate(); // Invalidate session
        return new ModelAndView("redirect:/login"); // Redirect to login page
    }
}

