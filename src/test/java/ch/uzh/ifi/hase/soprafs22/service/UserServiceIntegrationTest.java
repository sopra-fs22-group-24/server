package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
  }

  @Test
  public void createUser_validInputs_success() {
    // given
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    //Lea: password und name ausgetauscht
    testUser.setPassword("testName");
    testUser.setUsername("testUsername");

    // when
    User createdUser = userService.createUser(testUser);

    // then
    assertEquals(testUser.getId(), createdUser.getId());
    //Lea: password und name ausgetauscht
    assertEquals(testUser.getPassword(), createdUser.getPassword());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    User testUser = new User();
    //Lea: password und name ausgetauscht
    testUser.setPassword("testName");
    testUser.setUsername("testUsername");
    User createdUser = userService.createUser(testUser);

    // attempt to create second user with same username
    User testUser2 = new User();

    // change the name but forget about the username
    //Lea: password und name ausgetauscht
    testUser2.setPassword("testName2");
    testUser2.setUsername("testUsername");

    // check that an error is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
  }

  @Test
  public void login_whenUserNotExists_thenThrow() {
      User u = new User();
      u.setUsername("i do not exist");
      ResponseStatusException e = assertThrows(ResponseStatusException.class,() -> userService.login(u));
      assertEquals(HttpStatus.UNAUTHORIZED,e.getStatus());
  }

  @Test
    public void login_whenPasswordWrong_thenThrow() {
      User u = new User();
      u.setUsername("i can't remember my pw");
      u.setPassword("very save");
      u.setStatus(UserStatus.ONLINE);
      u.setToken("a token");
      userRepository.saveAndFlush(u);
      u.setPassword("save very");
      ResponseStatusException e = assertThrows(ResponseStatusException.class,() -> userService.login(u));
      assertEquals(HttpStatus.UNAUTHORIZED,e.getStatus());
    }

    @Test
    public void login_whenSuccess_thenChangeTokenAndSetStatusToOnline() {
        User u = new User();
        u.setUsername("i can't remember my pw");
        u.setPassword("very save");
        u.setStatus(UserStatus.OFFLINE);
        u.setToken("a token");
        userRepository.saveAndFlush(u);
        userService.login(u);
        User userAfterLogin = userRepository.findByUsername(u.getUsername());
        assertEquals(UserStatus.ONLINE, userAfterLogin.getStatus());
        assertNotEquals(u.getToken(), userAfterLogin.getToken());
    }

    @Test
    public void logout_whenSuccess_thenChangeUserStatusToOffline() {
        User u = new User();
        u.setUsername("i can't remember my pw");
        u.setPassword("very save");
        u.setStatus(UserStatus.ONLINE);
        u.setToken("a token");
        userRepository.saveAndFlush(u);
        userService.logout(u);
        User userAfterLogout = userRepository.findByUsername(u.getUsername());
        assertEquals(UserStatus.OFFLINE, userAfterLogout.getStatus());
    }

    @Test
    public void updateUser_whenNewUsernameAlreadyTaken_thenThrow() {
        User u1 = new User();
        u1.setUsername("uniqueName");
        u1.setPassword("very save");
        u1.setStatus(UserStatus.ONLINE);
        u1.setToken("a token");
        User u2 = new User();
        u2.setUsername("i wanna steal names");
        u2.setPassword("very save");
        u2.setStatus(UserStatus.ONLINE);
        u2.setToken("a token2");
        userRepository.saveAndFlush(u1);
        u2 = userRepository.saveAndFlush(u2);
        u2.setUsername("uniqueName");

        User finalU = u2;
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> userService.updateUser(finalU.getId(), finalU));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    public void updateUser_whenPasswordIsSetToNull_thenKeepPassword() {
        User u = new User();
        u.setUsername("i wanna steal names");
        u.setPassword("very save");
        String oldPassword = u.getPassword();
        String oldUserName = u.getUsername();
        u.setStatus(UserStatus.ONLINE);
        u.setToken("a token2");
        userRepository.saveAndFlush(u);
        u = userRepository.saveAndFlush(u);
        u.setPassword(null);
        u.setUsername("hui");
        User finalU = u;
        long userId = u.getId();

        userService.updateUser(finalU.getId(),finalU);
        User afterUpdate = userRepository.findById(userId);
        assertTrue(oldPassword.equals(afterUpdate.getPassword()));
        assertFalse(oldUserName.equals(afterUpdate.getUsername()));
    }


}
