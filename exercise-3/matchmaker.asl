// Agent matchmaker in project exercise3.mas2j

/* Initial beliefs and rules */

/* Initial goals */

/*
buyers(rabbit,[buyer]).
*/

/* Plans */

/*
+!getBuyers(Product,Trader) : true <- ?buyers(Product,P);
                                      .send(Trader,achieve,setBuyers(Product,P)).
*/

+!getBuyers(Product,Trader) : true <-
 .print(Trader, " is looking for product ", Product);
  envGetBuyers(Product,Trader).

+setBuyers(Buyers,Product,Trader) : true <-
 .print("Buyers ", Buyers, " want to buy ", Product, " from ", Trader);
 .send(Trader, achieve, setBuyers(Product, Buyers)).

