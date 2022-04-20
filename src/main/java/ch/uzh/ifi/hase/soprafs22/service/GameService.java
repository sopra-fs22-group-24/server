package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.Player;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.*;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.UserNotLobbyAdminException;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.*;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.NCardsDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Service
@Transactional
public class GameService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final GameRepository gameRepository;
    private final MessageService messageService;
    private final LobbyService lobbyService;

    @Autowired
    public GameService(GameRepository gameRepository, MessageService messageService, LobbyService lobbyService) {
        this.gameRepository = gameRepository;
        this.messageService = messageService;
        this.lobbyService = lobbyService;
    }





    public Game createGame(long lobbyId, User user) {

        // get lobby
        Lobby lobby = lobbyService.findByLobbyId(lobbyId);

        // check if user is creator/admin of lobby
        if(!lobby.userIsAdmin(user)) {
            throw new UserNotLobbyAdminException();
        }
        Game game = new Game();
        Vector<Player> players = new Vector<>();
        // transform all users of lobby to players
        Deck deck = new Deck();

        for(User u: lobby.getPlayers()) {
            Player player = new Player();
            player.setUser(u);
            Hand hand = new Hand();
            for(int i=0; i<7; i++) {
                hand.addCard(deck.drawCard());
            }
            player.setHand(hand);
            players.add(player);
        }
        //TODO Check how many players there are and throw accordingly
        DiscardPile discardPile = new DiscardPile();
        discardPile.discardCard(deck.drawCard());

        game.setPlayers(players);
        game.setDeck(deck);
        game.setDiscardPile(discardPile);

        Game savedGame = gameRepository.save(game);
        gameRepository.flush();
        return savedGame;


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
        messageService.sendToGame(game.getGameId(), "topMostCard",cardDTO);

        //Inform game which player has their turn now
        UserGetDTO userDTO = new UserGetDTO();
        userDTO.setUsername(player.getUser().getUsername());
        messageService.sendToGame(game.getGameId(), "playerTurn", userDTO);
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


    /*
    After all users joined the initialize will send relevant data (initial hand, first card on discard pile, etc.) to users
     */
    public void initialize(long gameId, User user) {
        Game game = gameRepository.findByGameId(gameId);
        authenticateUser(user, game);

        // send topMostCard on discard pile
        Card topMostCard = game.getDiscardPile().getTopmostCard();
        CardDTO topMostCardDTO = DTOMapper.INSTANCE.convertCardToCardDTO(topMostCard);
        messageService.sendToGame(gameId, "topMostCard", topMostCardDTO);

        // send cards to specific players and number of cards to all players
        for(Player player: game.getPlayers()) {
            Hand hand = player.getHand();
            // Send nCards to all players
            NCardsDTO nCardsDTO = new NCardsDTO();
            nCardsDTO.setUsername(player.getUser().getUsername());
            nCardsDTO.setnCards(hand.getCardCount());
            // NCardsDTO nCardsDTO = DTOMapperImpl.INSTANCE.convertEntityToNCardsDTO(player.getUser(),hand.getCardCount());
            messageService.sendToGame(gameId, "playerHasNCards", nCardsDTO);

            //send cards to specific player
            List<CardDTO> cardDTOS = new ArrayList<>();
            for(Card card : hand.getCards()) {
                CardDTO cardDTO = DTOMapper.INSTANCE.convertCardToCardDTO(card);
                cardDTOS.add(cardDTO);
            }
            messageService.sendToUser(player.getUser().getPrincipalName(),gameId+"/cards", cardDTOS);

        }

    }
}
