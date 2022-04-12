package ch.uzh.ifi.hase.soprafs22.rest.mapper;

import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

  @Mapping(source = "password", target = "password")
  @Mapping(source = "username", target = "username")
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "score", target = "score")
  @Mapping(source = "gamesWon", target = "gamesWon")
  @Mapping(source = "gamesPlayed", target = "gamesPlayed")
  UserGetDTO convertEntityToUserGetDTO(User user);

  @Mapping(source = "color", target = "color")
  @Mapping(source = "symbol", target = "symbol")
  CardDTO convertCardToCardDTO(Card card);

  @Mapping(source = "lobbyId", target = "lobbyId")
  LobbyPostDTO convertEntityToLobbyPostDTO(Lobby lobby);

  @Mapping(source = "lobbyId", target = "lobbyId")
  @Mapping(source = "players", target = "players")
  LobbyGetDTO convertEntityToLobbyGetDTO(Lobby lobby);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "token", target = "token")
    //returns to much information for login maybe we dont even need id & username aswell
    /*@Mapping(source = "password", target = "password")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "score", target = "score")
    @Mapping(source = "gamesWon", target = "gamesWon")
    @Mapping(source = "gamesPlayed", target = "gamesPlayed")*/
  UserGetTokenDTO convertEntityToUserGetTokenDTO(User user);

  @Mapping(source = "token", target = "token")
  User convertUserPostTokenDTOtoEntity(UserPostTokenDTO userPostTokenDTO);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "token", target = "token")
    User convertUserPutDTOToEntity(UserPutDTO userPutDTO);

    @Mapping(source = "gameId", target = "gameId")
    GameIdDTO convertGameToGameIdDTO(Game game);
}
