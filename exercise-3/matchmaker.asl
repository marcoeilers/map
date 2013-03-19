// Agent matchmaker in project exercise3.mas2j

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!registerOffer(Product,Trader) : true <-
 envAddOffer(Product,Trader).

+!registerRequest(Product,Trader) : true <-
 envAddRequest(Product,Trader).

+!getBuyers(Product,Trader) : true <-
 .print(Trader, " wants to sell ", Product);
  envGetBuyers(Product,Trader).

+setBuyers(Buyers,Product,Trader) : true <-
 .print("Buyers ", Buyers, " want to buy ", Product, " from ", Trader);
 .send(Trader, achieve, setBuyers(Product, Buyers)).
 
+!getSellers(Product,Trader) : true <-
 .print(Trader, " wants to buy product ", Product);
 envGetSellers(Product, Trader).

+setSellers(Sellers,Product,Trader) : true <-
 .print("Sellers ", Sellers, " want to sell ", Product, " to ", Trader);
 .send(Trader, achieve, setSellers(Product, Sellers)).

