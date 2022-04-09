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
import org.springframework.web.server.ResponseStatusException;

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

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);

    checkIfUserExists(newUser);

    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the password
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
    //Lea: password und name ausgetauscht
    User userByPassword = userRepository.findByPassword(userToBeCreated.getPassword());
    //Lea: password und name ausgetauscht
    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null && userByPassword != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(baseErrorMessage, "username and the password", "are"));
    } else if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "username", "is"));
    } else if (userByPassword != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(baseErrorMessage, "password", "is"));
    }
  }

  public User authenticateUser(String token) {
      User user = userRepository.findByToken(token);
      if(user == null || token == null) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
      }

      return user;

  }

    public User authenticateUser(StompHeaderAccessor accessor) {
        String token = StompHeaderUtil.getNativeHeaderField(accessor, "token");
        return authenticateUser(token);

    }

    public User login(User userInput) {
        //get saved user from provided username
        System.out.println(userInput.getUsername());
        System.out.println(userInput.getPassword());
        User savedUser = userRepository.findByUsername(userInput.getUsername());//findSpecificUserByName(userInput.getUsername());
        if(savedUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User doesn't exists");
        }

        if(!userInput.getPassword().equals(savedUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Passwords don't match");
        }
        //Create new token
        savedUser = updateToken(savedUser);
        savedUser.setStatus(UserStatus.ONLINE);
        return savedUser;
    }

    /**
     * Updates the token for a User
     * @param user
     * @return User
     */
    public User updateToken(User user) {
        user.setToken(UUID.randomUUID().toString());
        user = userRepository.save(user);
        log.debug("updated token for User: {}",user);
        return user;
    }

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
}
