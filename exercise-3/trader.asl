// Agent trader in project exercise3.mas2j

/* Initial beliefs and rules */

/* Initial goals */
offers(rabbit,20).

empty([],true).

findBestSale(Product,Best) :- 
 sales(Product,Buyers) & 
 bestSale(Product,Buyers,null,0,Best).
							  
bestSale(Product,[],BestBuyer,BestPrice,BestBuyer).
bestSale(Product,[First|Rest],BestBuyer,BestPrice,Best) :-
 lastPrice(Product,First,LastPrice) & 
 LastPrice > BestPrice & 
 bestSale(Product,Rest,First,LastPrice,Best).
bestSale(Product,[First|Rest],BestBuyer,BestPrice,Best) :-
 lastPrice(Product,First,LastPrice) & 
 LastPrice <= BestPrice & 
 bestSale(Product,Rest,BestBuyer,BestPrice,Best).
 
findBestNegotiation(Product,Best) :- 
 negotiations(Product,Sellers) & 
 bestNegotiation(Product,Sellers,null,0,Best).
							  
bestNegotiation(Product,[],BestSeller,BestPrice,BestSeller).
bestNegotiation(Product,[First|Rest],BestSeller,BestPrice,Best) :-
 lastPrice(Product,First,LastPrice) & 
 LastPrice > BestPrice & 
 bestNegotiation(Product,Rest,First,LastPrice,Best).
bestNegotiation(Product,[First|Rest],BestSeller,BestPrice,Best) :-
 lastPrice(Product,First,LastPrice) & 
 LastPrice >= BestPrice & 
 bestNegotiation(Product,Rest,BestSeller,BestPrice,Best).


/* Plans for Seller */

+offers(Product,Price) : true <- 
 .my_name(Me);
 .send(matchmaker,achieve,registerOffer(Product,Me));
 .send(matchmaker,achieve,getBuyers(Product,Me)).
								 
+!setBuyers(Product,Buyers) : empty(Buyers) <- .print("No Buyers for now.").
+!setBuyers(Product,Buyers) : not empty(Buyers) <- 
 +sales(Product,Buyers);
 !setupSales(Product,Buyers).

+!setupSales(Product,[]) : findBestSale(Product,Best) & not (Best = null) <-
 !makeSaleOffer(Product,Best).
+!setupSales(Product,[First|Rest]) : true <-
 ?offers(Product,Price);
 +lastPrice(Product,First,Price * 2);
 !setupSales(Product,Rest).				
	  
+!makeSaleOffer(Product,Buyer) : true <-
 .my_name(Me);
 ?lastPrice(Product,Buyer,OldPrice);
 ?offers(Product,MinPrice);
 +waitingFor(Product,Buyer);
 .send(Buyer,achieve,reactToBuyOffer(Product,Me,MinPrice)).
 
+!reactToSaleOffer(Product,Buyer,Price) : not lastPrice(Product,Buyer,LastPrice) <-
 .print("dont know this one yet").
 
+!reactToSaleOffer(Product,Buyer,Price) : lastPrice(Product,Buyer,LastPrice) <-
 .print("i know this one!").
 
/* Plans for Buyer */

+requests(Product,Price) : true <- 
 .my_name(Me);
 .send(matchmaker,achieve,registerRequest(Product,Me));
 .send(matchmaker,achieve,getSellers(Product,Me)).
								 
+!setSellers(Product,Sellers) : empty(Sellers) <- .print("No Sellers for now.").
+!setSellers(Product,Sellers) : not empty(Sellers) <- 
 +sales(Product,Sellers);
 !setupNegotiations(Product,Sellers).

+!setupNegotiations(Product,[]) : findBestNegotiation(Product,Best) & not (Best = null) <-
 !makeBuyOffer(Product,Best).
+!setupNegoriations(Product,[First|Rest]) : true <-
 ?requests(Product,Price);
 +lastPrice(Product,First,Price / 2);
 !setupNegotiations(Product,Rest).				
	  
+!makeBuyOffer(Product,Seller) : true <-
 .my_name(Me);
 ?lastPrice(Product,Seller,OldPrice);
 ?requests(Product,MaxPrice);
 +waitingFor(Product,Seller);
 .send(Seller,achieve,reactToSaleOffer(Product,Me,MaxPrice)).
 
+!reactToBuyOffer(Product,Seller,Price) : not lastPrice(Product,Seller,LastPrice) <-
 .print("dont know this one yet").
 
+!reactToBuyOffer(Product,Seller,Price) : lastPrice(Product,Seller,LastPrice) <-
 .print("i know this one!").

