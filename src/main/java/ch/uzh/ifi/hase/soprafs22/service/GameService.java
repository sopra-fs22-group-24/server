package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.Player;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.*;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.UserNotLobbyAdminException;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.*;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.*;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.utils.Globals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

@Service
@Transactional
public class GameService {
    private final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final MessageService messageService;
    private final LobbyService lobbyService;
    private final Random random;
    private final UserService userService;

    @Autowired
    public GameService(GameRepository gameRepository, MessageService messageService, LobbyService lobbyService, UserService userService, @Value("#{new java.util.Random()}") Random random) {
        this.gameRepository = gameRepository;
        this.messageService = messageService;
        this.lobbyService = lobbyService;
        this.userService = userService;
        this.random = random;
    }





    public Game createGame(long lobbyId, User user) {

        // get lobby
        Lobby lobby = lobbyService.findByLobbyId(lobbyId);
        if(lobby.getGame() != null) {
            return lobby.getGame();
        }
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
        game.setSudo(false);
        Game savedGame = gameRepository.save(game);
        lobbyService.setGame(lobby,game);
        gameRepository.flush();
        return savedGame;


    }

    public void playCard(long gameId, User user, Card card, User otherUser, boolean uno) {
        // TODO check out if there are python like decorators for error handling
        //check game
        Game game = getGameFromGameId(gameId);

        //check player
        Player player = authenticateUser(user, game);
        changePrincipal(user, player, game);


        //check if it is the players turn
        if(!playersTurn(player, game)) {
            throw new NotPlayerTurnException();
        }
        //check player has card in hand he wants to play
        if(!hasCardInHand(player, card)) {
            throw new CardNotInHandException();
        }
        //check card can be played
        if(!cardCanBePlayed(game.getDiscardPile(),card)) {
            if(!game.getSudo()) {
                throw new CardNotPlayableException();
            }
        }
        // handle uno
        // reset uno
        //set uno if applicable
        handleUno(game, player, uno);
        if(uno){
            if(checkUnoCanBeCalled(player)) {
                player.setHasSaidUno(true);
            }
        } else {
            player.setHasSaidUno(false);
        }

        Player lastPlayer = game.getLastPlayer();

        if(card.getSymbol() == Symbol.WILDCARD) {
            handleWildcard(game, player,card);
        } else if (card.getSymbol() == Symbol.EXTREME_HIT) {
            handleExtremeHit(game, player, card, otherUser);
        } else if(card.getSymbol() == Symbol.DISCARD_ALL) {
            handleDiscardAll(game, player, card);
        } else if (card.getSymbol() == Symbol.SKIP) {
            handleSkip(game, player, card);

        } else if (card.getSymbol() == Symbol.HIT_2) {
            handleHit2(game, player, card);
        } else if (card.getSymbol() == Symbol.REVERSE) {
            handleReverse(game, player, card);
        } else {
            handleNormalCard(game, player, card);
        }


        // set uno to true for last player
        lastPlayer.setHasSaidUno(true);
        // persist changes to game from move
        gameRepository.saveAndFlush(game);
        //update the player/players State here because always topmost card & nrOfcardPlayerx and next turn called
        informPlayers_TopMostCard(game,game.getDiscardPile().getTopmostCard());
        informPlayers_nrOfCardsInHandPlayers(game);
        informPlayerOnHand(player, game);
        //check win
        if (player.getHand().getCardCount() == 0) {
            handleWin(game, player);
            return;
        }
        informPlayerToTurn(game);



    }

    private void handleUno(Game game, Player player, boolean uno) {
        if(uno){
            if(checkUnoCanBeCalled(player)) {
                player.setHasSaidUno(true);
                UserGetDTO userGetDTO = new UserGetDTO();
                userGetDTO.setUsername(player.getUser().getUsername());
                messageService.sendToGame(game.getGameId(),"saidUno", userGetDTO);
            }
        } else {
            player.setHasSaidUno(false);
        }


    }

    private void handleWin(Game game, Player player) {
        /*
        int score = 0;
        for(Player p : game.getPlayers()) {
            for(Card card : p.getHand().getCards()) {
                score += card.getSymbol().getScore();
            }
        }

        //set score
        int oldScore = player.getScore();
        player.setScore(oldScore+score);
        gameRepository.saveAndFlush(game);
        //send scores to players
        List<ScoreDTO> scoreDTOS = new ArrayList<>();
        for(Player p : game.getPlayers()) {
            ScoreDTO scoreDTO = new ScoreDTO();
            scoreDTO.setUsername(p.getUser().getUsername());
            scoreDTO.setScore(p.getScore());
            scoreDTOS.add(scoreDTO);
        }

        messageService.sendToGame(game.getGameId(), "score", scoreDTOS);
        */

        //increase games played for each user
        for (Player p : game.getPlayers()) {
            User user = p.getUser();
            userService.increaseGamesPlayed(user);
        }

        //increase games won for winner
        userService.increaseGamesWon(player.getUser());

        //inform players who won
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(player.getUser());
        messageService.sendToGame(game.getGameId(), "gameEnd", userGetDTO);

        //remove game from lobby
        lobbyService.destroyLobby(game);

        // remove game
        gameRepository.delete(game);


    }

    private void informPlayerOnHand(Player player, Game game) {
        List<CardDTO> playerHand = new ArrayList<>();
        for(Card playerCard: player.getHand().getCards()) {
            playerHand.add(DTOMapper.INSTANCE.convertCardToCardDTO(playerCard));
        }
        messageService.sendToUser(player.getUser().getPrincipalName(),game.getGameId()+"/playedCard", playerHand);
    }

    private void handleReverse(Game game, Player player, Card card) {
        // remove card from player hand
        player.getHand().removeCard(card);

        //place card in discard pile
        game.getDiscardPile().discardCard(card);
        //change turn direction
        game.reverseTurndirection();
        //increase turn
        game.nextTurn();
    }

    private void handleHit2(Game game, Player player, Card card) {
        //TODO handle Hit_2 case when next player wants to HIT2 aswell

        //next player has to draw 2 times before next players turn
        // find next player
        Player victim = game.getNextPlayer();
        // Let them draw twice
        int cardsBefore = victim.getHand().getCardCount();
        List<CardDTO> cardDTOS1 = playerDrawsCard(game, victim);
        List<CardDTO> cardDTOS2 = playerDrawsCard(game, victim);
        cardDTOS1.addAll(cardDTOS2);
        int cardsDrawn = victim.getHand().getCardCount()-cardsBefore;
        //remove card from player hand
        player.getHand().removeCard(card);
        //set card on Top
        game.getDiscardPile().discardCard(card);
        game.nextTurn();
        // send drawn cards
        List<CardDTO> cardDTOS = new ArrayList<>();
        for(Card playerCard: victim.getHand().getCards()) {
            cardDTOS.add(DTOMapper.INSTANCE.convertCardToCardDTO(playerCard));
        }
        messageService.sendToUser(victim.getUser().getPrincipalName(),game.getGameId()+"/cardsDrawn", cardDTOS);
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMsg(String.format("%s fell victim to a Hit 2 and had to draw %d cards", victim.getUser().getUsername(), cardsDrawn));
        messageService.sendToGame(game.getGameId(), "messages", messageDTO);
        game.nextTurn();
    }

    private void handleSkip(Game game, Player player, Card card) {
        // remove card & set it on top
        player.getHand().removeCard(card);
        game.getDiscardPile().discardCard(card);
        // skip next players turn
        game.nextTurn();
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMsg(String.format("%s was skipped", game.getPlayerTurn().getUser().getUsername()));
        messageService.sendToGame(game.getGameId(), "messages", messageDTO);
        game.nextTurn();
    }

    private void handleDiscardAll(Game game, Player player, Card card) {
        // TODO input discard all card 4 cards exists each color once
        // discards all cards of that color from user
        // discard all doesnt work if it leads to instant win
        //remove discard all Card can be played anyways and Set it on discardpile
        player.getHand().removeCard(card);
        game.getDiscardPile().discardCard(card);
        //check if it leads to instantwin witout possible unocall
        int numberOfCards=0;
        int numberOfCardsToDiscard =0;
        for (Card cardToCount : player.getHand().getCards()
        ) {
            ++numberOfCards;
            if (cardToCount.getColor()==card.getColor()){
                ++numberOfCardsToDiscard;
            }

        }
        int cardsLeftInHand = numberOfCards-numberOfCardsToDiscard;
        //if after discarding discardAllCard all other cards in Hand are discarded it is not allowed because it grants instantwin
        List<Card> toDiscard = new ArrayList();

        if (cardsLeftInHand > 0) {
            for (Card cardToCheck : player.getHand().getCards()) {
                if (card.getColor() == cardToCheck.getColor()) {
                    if(cardToCheck.getSymbol() != Symbol.DISCARD_ALL) {
                        toDiscard.add(cardToCheck);
                    }
                }
            }
        }
        /*
        else {
            toDiscard.add(card);
        }*/
        for(Card cardToDiscard: toDiscard) {
            player.getHand().removeCard(cardToDiscard);
            game.getDiscardPile().discardCard(cardToDiscard);
        }
        // discard DiscardAll card at the end
        player.getHand().removeCard(card);
        game.getDiscardPile().discardCard(card);
        // check if there is a second discardAll of the same color and discard it
        boolean foundAnother = false;
        for (Card cardToDiscard: player.getHand().getCards()) {
            if (cardToDiscard.getSymbol() == Symbol.DISCARD_ALL && cardToDiscard.getColor() == card.getColor()) {
                card = cardToDiscard;
            }
        }
        if(foundAnother) {
            player.getHand().removeCard(card);
            game.getDiscardPile().discardCard(card);
        }

        game.nextTurn();
    }

    private void handleExtremeHit(Game game, Player player, Card card, User otherUser) {
        if(card.getColor()==null){
            throw new CardColorNotChoosenException();
        }
        Player victim;
        try {
            victim = game.getPlayerFromUser(otherUser);
        } catch(NullPointerException e) {
            throw new PlayerNotInGameException();
        }
        if(player.getUser().getId().equals(victim.getUser().getId())) {
            throw new CantTargetYourselfException();
        }
        //remove card from hand
        player.getHand().removeCard(card);
        //set card on Top
        game.getDiscardPile().discardCard(card);
        //choosen Player draws
        int cardsBefore = victim.getHand().getCardCount();
        playerDrawsCard(game, victim);
        int cardsDrawn = victim.getHand().getCardCount() - cardsBefore;
        List<CardDTO> cardDTOS = new ArrayList<>();
        for(Card playerCard: victim.getHand().getCards()) {
            cardDTOS.add(DTOMapper.INSTANCE.convertCardToCardDTO(playerCard));
        }
        //send drawn cards to player
        messageService.sendToUser(victim.getUser().getPrincipalName(),game.getGameId()+"/cardsDrawn", cardDTOS);
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMsg(String.format("%s fell victim to an Extreme Hit from %s and has drawn %d cards",victim.getUser().getUsername(), player.getUser().getUsername(), cardsDrawn));
        messageService.sendToGame(game.getGameId(), "messages", messageDTO);
        //messageService.sendToUser(victim.getUser().getPrincipalName(),game.getGameId()+"/cardsDrawn", cardDTOS);

        game.nextTurn();
    }

    private void handleWildcard(Game game, Player player,Card card) {
        // input wildcard + color taken from card attribute
        // topmost card color set to choosen color
        //remove card from players hand
        //set choosen color
        // is already set in Attribute color in client but check if not null
        if(card.getColor()==null){
            throw new CardColorNotChoosenException();

        }
        player.getHand().removeCard(card);
        //set card on Top
        game.getDiscardPile().discardCard(card);
        game.nextTurn();
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

    }

    private boolean cardCanBePlayed(DiscardPile discardPile, Card card) {


        if(card.getSymbol() == Symbol.WILDCARD ||
                card.getSymbol() == Symbol.EXTREME_HIT) {
            return true;
        }

        if(discardPile.getTopmostCard().getColor() == card.getColor()) {
            return true;
        }
        if(discardPile.getTopmostCard().getSymbol() == card.getSymbol()) {
            return true;
        }
        // If first card is wildcard or extreme hit then this ensures any card can be played
        if(discardPile.getTopmostCard().getColor() == Color.NULL) {
            return true;
        }
        return false;
    }

    private Game getGameFromGameId(long gameId) {
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

    private boolean hasCardInHand(Player player, Card card) {
        return player.getHand().containsCard(card);
    }
    private boolean checkUnoCanBeCalled(Player player){
        return player.getHand().getCardCount()<=2;
    }
    // TODO remember if uno was called by the last player whos turn it was

    /*
    After all users joined the initialize will send relevant data (initial hand, first card on discard pile, etc.) to users
     */
    public void initialize(long gameId, User user) {
        Game game = gameRepository.findByGameId(gameId);
        authenticateUser(user, game);
        Player p = game.getPlayerFromUser(user);
        changePrincipal(user, p, game);
        // send topMostCard on discard pile
        Card topMostCard = game.getDiscardPile().getTopmostCard();
        CardDTO topMostCardDTO = DTOMapper.INSTANCE.convertCardToCardDTO(topMostCard);
        messageService.sendToGame(gameId, "topMostCard", topMostCardDTO);
        informPlayers_nrOfCardsInHandPlayers(game);
        // send cards to specific players and number of cards to all players
        for(Player player: game.getPlayers()) {
            Hand hand = player.getHand();
            // Send nCards to all players
            /*
            NCardsDTO nCardsDTO = new NCardsDTO();
            nCardsDTO.setUsername(player.getUser().getUsername());
            nCardsDTO.setnCards(hand.getCardCount());
            messageService.sendToGame(gameId, "playerHasNCards", nCardsDTO);
            */
            //send cards to specific player
            List<CardDTO> cardDTOS = new ArrayList<>();
            for(Card card : hand.getCards()) {
                CardDTO cardDTO = DTOMapper.INSTANCE.convertCardToCardDTO(card);
                cardDTOS.add(cardDTO);
            }
            messageService.sendToUser(player.getUser().getPrincipalName(),gameId+"/playedCard", cardDTOS);

        }
        //inform which players turn it is
        Player playerTurn = game.getPlayerTurn();
        messageService.sendToGame(gameId, "playerTurn", DTOMapper.INSTANCE.convertEntityToUserGetDTO(playerTurn.getUser()));

    }

    private void changePrincipal(User user, Player player, Game game) {
        if(user.getPrincipalName().equals(player.getUser().getPrincipalName())) {
            return;
        }
        player.getUser().setPrincipalName(user.getPrincipalName());
        gameRepository.saveAndFlush(game);
    }

    public void drawCard(long gameId, User user) {
        Game game = getGameFromGameId(gameId);
        Player player = authenticateUser(user, game);
        changePrincipal(user, player, game);

        //check if it is the players turn
        if(!playersTurn(player, game)) {
            throw new NotPlayerTurnException();
        }

        Player lastPlayer = game.getLastPlayer();
        int cardsBefore = player.getHand().getCardCount();
        playerDrawsCard(game, player);
        int cardsDrawn = player.getHand().getCardCount()-cardsBefore;

        List<CardDTO> cardsDtos = new ArrayList<>();
        for(Card playerCard: player.getHand().getCards()) {
            cardsDtos.add(DTOMapper.INSTANCE.convertCardToCardDTO(playerCard));
        }
        // inform players of the card count of the drawing player
        NCardsDTO nCardsDTO = new NCardsDTO();
        nCardsDTO.setUsername(user.getUsername());
        nCardsDTO.setnCards(player.getHand().getCardCount());
        messageService.sendToGame(gameId, "playerHasNCards", nCardsDTO);

        // inform player of the new cards they got
        messageService.sendToUser(player.getUser().getPrincipalName(),gameId+"/cardsDrawn", cardsDtos);

        // increase turn and inform players
        game.nextTurn();
        lastPlayer.setHasSaidUno(true);
        gameRepository.saveAndFlush(game);
        Player playerTurn = game.getPlayerTurn();
        messageService.sendToGame(gameId, "playerTurn", DTOMapper.INSTANCE.convertEntityToUserGetDTO(playerTurn.getUser()));

        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMsg(String.format("%s pressed the launcher button and has drawn %d cards", player.getUser().getUsername(), cardsDrawn));
        messageService.sendToGame(gameId, "messages", messageDTO);

    }

    private List<CardDTO> playerDrawsCard(Game game, Player player) {
        /*
        TODO: adjust range, maybe don't use a uniform distribution but something more akin to:
        0 cards: 30%, 1 card: 20%, 2 cards: 10%, 3 cards: 5% ....
         */

        int max = Globals.maxDrawCount();
        int min = Globals.minDrawCount();
        int[] distribution = {0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,5,5,5,6,6,7,7,8,9,10,11,12};

        //int randomCardAmount = (int) ((Math.random() * (max - min)) + min);
        int randomCardAmount = distribution[random.nextInt(distribution.length)];

        List<CardDTO> cardDTOS = new ArrayList<>();
        for(int i=0; i< randomCardAmount;i++) {
            //check if deck is empty else refill it
            if(game.getDeck().deckIsEmpty()){
                game.getDeck().shuffle(game.getDiscardPile().emptyDiscardPileExceptTopMostCard());
            }
            Card card = game.getDeck().drawCard();
            player.getHand().addCard(card);
            cardDTOS.add(DTOMapper.INSTANCE.convertCardToCardDTO(card));
        }
        return cardDTOS;
    }
    // send to message to all players to update topmost card
    private void informPlayers_TopMostCard(Game game, Card card){
        CardDTO cardDTO = new CardDTO();
        cardDTO.setColor(card.getColor());
        cardDTO.setSymbol(card.getSymbol());
        messageService.sendToGame(game.getGameId(), "topMostCard",cardDTO);
    }
    //update nrOfCardsInHand for other players after player played a card
    private void informPlayers_nrOfCardsInHandPlayers(Game game){
        List<NCardsDTO> nCardsDTOS = new ArrayList<>();
        for(Player player: game.getPlayers()) {
            NCardsDTO nCardsDTO = new NCardsDTO();
            nCardsDTO.setUsername(player.getUser().getUsername());
            nCardsDTO.setnCards(player.getHand().getCardCount());
            nCardsDTOS.add(nCardsDTO);
        }
        Long gameId = game.getGameId();
        messageService.sendToGame(gameId, "playerHasNCards", nCardsDTOS);
    }
    // informPlayersTurn
    private void informPlayerToTurn(Game game){
        Long gameId = game.getGameId();
        Player playerTurn = game.getPlayerTurn();
        messageService.sendToGame(gameId, "playerTurn", DTOMapper.INSTANCE.convertEntityToUserGetDTO(playerTurn.getUser()));

    }

    public void callOutPlayer(long gameId, User user, User calledOutUser) {
        Game game = gameRepository.findByGameId(gameId);
        Player player = authenticateUser(user, game);
        changePrincipal(user, player, game);

        Player calledOutPlayer = authenticateUser(calledOutUser, game);

        //check if called out player has one card and not said uno, else throw
        if(calledOutPlayer.getHand().getCardCount() != 1 || calledOutPlayer.isHasSaidUno()) {
            throw new InvalidCallOutException();
        }
        //called out player has to draw twice
        playerDrawsCard(game, calledOutPlayer);
        playerDrawsCard(game, calledOutPlayer);

        List<CardDTO> cardDTOS = new ArrayList<>();
        for (Card card: calledOutPlayer.getHand().getCards()) {
            cardDTOS.add(DTOMapper.INSTANCE.convertCardToCardDTO(card));
        }
        gameRepository.saveAndFlush(game);
        informPlayers_nrOfCardsInHandPlayers(game);

        CalledOutDTO calledOutDTO = new CalledOutDTO();
        calledOutDTO.setCallee(player.getUser().getUsername());
        calledOutDTO.setCalledOutPlayer(calledOutPlayer.getUser().getUsername());
        messageService.sendToGame(gameId,"calledOut", calledOutDTO);
        messageService.sendToUser(calledOutPlayer.getUser().getPrincipalName(),gameId+"/cardsDrawn", cardDTOS);

    }

    public void updateUser(User user) {
        Game game = gameRepository.findByPlayers(user);
        if(game == null) {
            return;
        }
        for(Player savedPlayer: game.getPlayers()) {
            if(user.getId().equals(savedPlayer.getUser().getId())) {
                updatePrincipalName(game,savedPlayer, user);
            }
        }
    }

    private void updatePrincipalName(Game lobby,Player savedUser, User newUser) {
        if(!(savedUser.getUser().getPrincipalName().equals(newUser.getPrincipalName()))) {
            String oldPrincipal = savedUser.getUser().getPrincipalName();
            savedUser.getUser().setPrincipalName(newUser.getPrincipalName());
            gameRepository.saveAndFlush(lobby);
            log.info("updated user {}. Set principal name from {} to {}", savedUser.getUser().getUsername(), oldPrincipal, savedUser.getUser().getPrincipalName());
        }
    }

    public void enableSudo(long gameId) {
        Game game = gameRepository.findByGameId(gameId);
        game.setSudo(true);
        gameRepository.saveAndFlush(game);
    }

    public void sayUnoAfterPlaying(long gameId, User user) {
        Game game = gameRepository.findByGameId(gameId);
        Player player = authenticateUser(user, game);
        if(checkUnoCanBeCalled(player)) {
            player.setHasSaidUno(true);
            gameRepository.saveAndFlush(game);
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setMsg(String.format("%s said uno", player.getUser().getUsername()));
            messageService.sendToGame(gameId, "messages", messageDTO);
        }

    }
}
