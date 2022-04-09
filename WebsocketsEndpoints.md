# Websocket Endpoints


## Connection
Connect to "URL/ws-connect". A valid user token has to be sent via header so that we can connect the websocket session to the user.  
Example with socketConnection class in client:  
```    socket.connect(localStorage.getItem('token'));```

## Send endpoints
### /app/createLobby
Creates a lobby and returns lobbyId to `/queue/messages`.  
Example with socketConnection class in client:  
```        socket.send("/app/createLobby",{})``` 

### /app/joinLobby
Joins an existing lobby and returns Message to `/queue/messages`.  
Example with socketConnection class in client:  
```socket.send("/app/createLobby",JSON.stringify{"lobbyId":lobbyId})``` 


## Subscription endpoints
Subscription endpoints enable the communication between server and client.  
On subscription one has to provide an endpoint destination as well as a callback function to handle incomming messages.
### /users/queue/messages
Private endpoint for User (only a specific user will receive messages here)  
Messages here are as of yet not standardized.  

Example with socketConnection class in client:  
```socket.subscribe("/users/queue/messages", callBackFunction)```
