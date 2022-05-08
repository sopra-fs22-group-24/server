package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.utils.StompHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }
//get all users
  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  //create a new user
  public User createUser(User newUser) {
    if(null!=userRepository.findByUsername(newUser.getUsername())){
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Username is already taken");
    }
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

    //check login and login / else error
    public User login(User userInput) {
        //get saved user from provided username
        User savedUser = userRepository.findByUsername(userInput.getUsername());
        //check if useranme exists
        if(savedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid Login Credentials");
        }
        //check password
        if(!userInput.getPassword().equals(savedUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid Login Credentials");
        }
        //Create new token
        savedUser = updateToken(savedUser);
        savedUser.setStatus(UserStatus.ONLINE);
        return savedUser;
    }

    // logout a user else throw err
    public User logout(User userInput) {
        //authorize
        authenticateUser(userInput.getToken());
        //update status
        User userToBeLoggedOut = userRepository.findByToken(userInput.getToken());
        userToBeLoggedOut.setStatus(UserStatus.OFFLINE);
        userRepository.save(userToBeLoggedOut);
        userRepository.flush();
        //retrun updated User
        return userToBeLoggedOut;
    }

    //updates user data username/password (more can be added)
    public void updateUser(long id, User userinput) {
        //CHECK if authorized
        authenticateUser(userinput.getToken());
        //get user by id and check if id valid
        User userToBeUpdated = getUserById(id);
        if(null != userRepository.findByUsername(userinput.getUsername())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }
        else {
            userToBeUpdated.setUsername(userinput.getUsername());
        }
        if(userinput.getPassword()!= null){
            userToBeUpdated.setPassword(userinput.getPassword());
        }
        userRepository.save(userToBeUpdated);
        userRepository.flush();

    }

  //get a user by id throws err if not found
  public User getUserById(Long id) {
      Optional<User> userById = userRepository.findById(id);
      if (userById.isEmpty()) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User was not found");
      }
        return userById.get();
    }

// check token else error
  public User authenticateUser(String token) {
      User user = userRepository.findByToken(token);
      if(user == null || token == null) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
      }

      return user;

  }

    /**
     * Updates the token for a User
     * @param user
     * @return User
     */
    //refresh token for user
    public User updateToken(User user) {
        user.setToken(UUID.randomUUID().toString());
        user = userRepository.save(user);
        log.debug("updated token for User: {}",user);
        return user;
    }
    public  void setProfilePicture (long id, MultipartFile file) {
        User user = getUserById(id);
        try {
            user.setPicture(file.getBytes());
        }
        catch (IOException e) {
            throw new RuntimeException("profilpicture could npt be read in backend",e);
        }
        userRepository.save(user);
    }

    public byte[] getProfilePicture(long id){
        User user = getUserById(id);
        return user.getPicture();
    }

/*
    public User authenticateUser(StompHeaderAccessor accessor) {
        String token = StompHeaderUtil.getNativeHeaderField(accessor, "token");
        return authenticateUser(token);

    }
*/




    public User getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    public User getUserByPrincipalName(String name) {
        return userRepository.findByPrincipalName(name);
    }

    public void addPrincipalName(User user, String name) {
        user.setPrincipalName(name);

        userRepository.save(user);
        userRepository.flush();
    }

    public User getUserByUsername(String username) {return userRepository.findByUsername(username);}
}
