package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.entity.Player;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.*;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.*;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GameService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final GameRepository gameRepository;
    private final MessageService messageService;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository, MessageService messageService) {
        this.gameRepository = gameRepository;
        this.messageService = messageService;
    }


    public Game createGame(long lobbyId, User user) {

        // get lobby

        // check if user is creator/admin of lobby

        // transform all users of lobby to players

        // initialise game

        // return game
        /*
        Game game = new Game();


        Deck deck = new Deck();
        game.setDeck(deck);

        DiscardPile discardPile = new DiscardPile();
        game.setDiscardPile(discardPile);

        gameRepository.save(game);
        gameRepository.flush();

         */
        return null;
    }

    public void playCard(long gameId, User user, Card card) {
        // TODO check out if there are python like decorators for error handling
        //check game
        Game game = getGameFromGameId(gameId);

        //check player
        Player player = authenticateUser(user, game);


        //check if it is the players turn
        if(!playersTurn(player, game)) {
            throw new NotPlayerTurnException();
        }
        //check player has card in hand
        if(!hasCardInHand(player, card)) {
            throw new CardNotInHandException();
            //messageService.sendErrorToUser(user.getPrincipalName(), "CardNotInHand");
        }
        //check card can be played
        if(!cardCanBePlayed(game.getDiscardPile(),card)) {
            throw new CardNotPlayableException();
            //messageService.sendErrorToUser(user.getPrincipalName(), "CardCanNotBePlayed");
        }

        if(card.getSymbol() == Symbol.WILDCARD) {
            //TODO handle Wildcard
        }
        else if(card.getSymbol() == Symbol.DISCARD_ALL) {
            //TODO handle discard all
        } else if (card.getSymbol() == Symbol.EXTREME_HIT) {
            //TODO handle Extreme_hit
        } else if (card.getSymbol() == Symbol.HIT_2) {
            //TODO handle Hit_2
        } else if (card.getSymbol() == Symbol.REVERSE) {
            //TODO handle Reverse
        } else {
            handleNormalCard(game, player, card);
        }
    }

    private boolean playersTurn(Player player, Game game) {
        return game.checkPlayerTurn(player);
    }

    private void handleNormalCard(Game game, Player player, Card card) {
        // remove card from player hand
        player.getHand().removeCard(card);

        //place card in discard pile
        game.getDiscardPile().discardCard(card);

        //increase turn
        game.nextTurn();

        //inform game which card was played
        CardDTO cardDTO = new CardDTO();
        cardDTO.setColor(card.getColor());
        cardDTO.setSymbol(card.getSymbol());
        messageService.sendToGame(game.getGameId(), cardDTO);

        //Inform game which player has their turn now
        UserGetDTO userDTO = new UserGetDTO();
        userDTO.setUsername(player.getUser().getUsername());
        messageService.sendToGame(game.getGameId(), userDTO);
    }

    private boolean cardCanBePlayed(DiscardPile discardPile, Card card) {
        if(card.getSymbol() == Symbol.WILDCARD ||
                card.getSymbol() == Symbol.EXTREME_HIT ||
                card.getSymbol() == Symbol.DISCARD_ALL) {
            return true;
        }

        if(discardPile.getTopmostCard().getColor() == card.getColor()) {
            return true;
        }
        if(discardPile.getTopmostCard().getSymbol() == card.getSymbol()) {
            return true;
        }
        return false;
    }

    public Game getGameFromGameId(long gameId) {
        Game game = gameRepository.findByGameId(gameId);
        if (game == null) {
            throw new GameNotExistsException();

        }
        return game;

    }
    private Player authenticateUser(User user, Game game) {
        Player player = game.getPlayerFromUser(user);

        if(player == null) {
            throw new PlayerNotInGameException();
        }

        return player;
    }

    public boolean hasCardInHand(Player player, Card card) {
        if(player.getHand().containsCard(card)) {
            return true;
        }
        return false;
    }
}
