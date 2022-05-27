package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.rest.dto.*;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.mapping.Any;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers() {
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetTokenDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);

    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetTokenDTO(createdUser);
  }

    @PostMapping("/users/{id}/picture")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
        public void setProfilePicture(@RequestParam("picture") MultipartFile file, @PathVariable("id") long id) {

        userService.saveImageFile(id,file);
    }

    @GetMapping(
            value = "/users/{id}/picture"
    )
    public @ResponseBody String getprofilepicture(@PathVariable("id") Long id) {

      byte[] picture =  userService.getProfilePicture(id);
        String encodedString = Base64.getEncoder().encodeToString(picture);
      return encodedString;
    }
    @GetMapping(
            value = "/users/picture/{username}"
    )
    public @ResponseBody String getprofilepicture(@PathVariable("username") String username) {

        byte[] picture =  userService.getProfilePicture(username);
        String encodedString = Base64.getEncoder().encodeToString(picture);
        return encodedString;
    }

    @GetMapping(value = "/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUserByUserID(@PathVariable("id") String id) {
        //change id from string to long
        long idLong = Long.parseLong(id);
        //look for user by id
        //Optional because it is not certain that user exists. If not, return not found
        User user = userService.getUserById(idLong);

        //return user
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public UserGetTokenDTO login(@RequestBody UserPostDTO dto ) {

        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(dto);
        User loggedInUser = userService.login(userInput);
        return DTOMapper.INSTANCE.convertEntityToUserGetTokenDTO(loggedInUser);

    }

    @PostMapping(value = "/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void logout(@RequestBody UserPostTokenDTO dto ) {
        User userInput = DTOMapper.INSTANCE.convertUserPostTokenDTOtoEntity(dto);
        userService.logout(userInput);

    }

    @PutMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateUserInfo(@RequestBody UserPutDTO userPutDTO, @PathVariable("id") long id) {
    // converto to entitiy
        User userinput = DTOMapper.INSTANCE.convertUserPutDTOToEntity(userPutDTO);

      userService.updateUser(id, userinput);
    }

}
