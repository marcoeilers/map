// Agent matchmaker in project exercise3.mas2j

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// registers an offer with the ItemDB in the environment
+!registerOffer(Product,Trader) : true <-
 envAddOffer(Product,Trader).

// registers a request with the ItemDB in the environment
+!registerRequest(Product,Trader) : true <-
 envAddRequest(Product,Trader).
 
// removes an offer from the ItemDB in the environment
+!removeOffer(Product,Trader) : true <-
 envRemoveOffer(Product,Trader).
 
// removes a request from the ItemDB in the environment
+!removeRequest(Product,Trader) : true <-
 envRemoveRequest(Product,Trader).

// gets buyers for a specific item from the environment
+!getBuyers(Product,Trader) : true <-
 .print(Trader, " wants to sell ", Product);
 envGetBuyers(Product,Trader).

// gets triggered after envGetBuyers finished
+setBuyers(Buyers,Product,Trader) : true <-
 .print("Buyers ", Buyers, " want to buy ", Product, " from ", Trader);
 .send(Trader, achieve, setBuyers(Product, Buyers)).
 
// gets sellers for a specific item from the environment
+!getSellers(Product,Trader) : true <-
 .print(Trader, " wants to buy product ", Product);
 envGetSellers(Product, Trader).

// gets triggered after envGetSellers finished
+setSellers(Sellers,Product,Trader) : true <-
 .print("Sellers ", Sellers, " want to sell ", Product, " to ", Trader);
 .send(Trader, achieve, setSellers(Product, Sellers)).

