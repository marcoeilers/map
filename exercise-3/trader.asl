// Agent trader in project exercise3.mas2j

stepFactor(0.2).
minStep(0.1).

/* Initial beliefs and rules */

/* Initial goals */

//offers([],18).

empty([]).

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
 bestNegotiation(Product,Sellers,null,1000000,Best).

bestNegotiation(Product,[],BestSeller,BestPrice,BestSeller).
bestNegotiation(Product,[First|Rest],BestSeller,BestPrice,Best) :-
 lastPrice(Product,First,LastPrice) & 
 LastPrice < BestPrice & 
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
 .print("Got buyers.");
 +sales(Product,Buyers);
 !setupSales(Product,Buyers).

+!setupSales(Product,[First|Rest]) : true <-
 ?offers(Product,Price);
 +lastPrice(Product,First,Price * 2);
 !setupSales(Product,Rest).	
+!setupSales(Product,[]) : findBestSale(Product,Best) & not (Best = null) <-
 !makeSaleOffer(Product,Best,true).			

+!makeSaleOffer(Product,Buyer,true) : lastPrice(Product,Buyer,TODO) & not initialSent(Product,Buyer) <-
 .my_name(Me);
 ?lastPrice(Product,Buyer,OldPrice);
 ?offers(Product,MinPrice);
 +waitingFor(Product,Buyer);
 +initialSent(Product,Buyer);
 .print("sends initial offer",OldPrice);
 .send(Buyer,achieve,reactToBuyOffer(Product,Me,OldPrice,true)).
 
+!makeSaleOffer(Product,Buyer,false) : true <-
 .my_name(Me);
 ?lastPrice(Product,Buyer,OldPrice);
 ?offers(Product,MinPrice);
 +waitingFor(Product,Buyer);
 +initialSent(Product,Buyer);
 ?stepFactor(StepFactor);
 -lastPrice(Product,Buyer,OldPrice);
 +lastPrice(Product,Buyer,OldPrice - ((OldPrice-MinPrice)*StepFactor));
 .print("sends noninitial offer");
 .send(Buyer,achieve,reactToBuyOffer(Product,Me,OldPrice - ((OldPrice-MinPrice)*StepFactor),false)).
 
+!reactToSaleOffer(Product,Buyer,Price,true) : not lastPrice(Product,Buyer,LastPrice) <-
 ?offers(Product,MinPrice);
 +lastPrice(Product,Buyer,2 * MinPrice);
 !addSale(Product,Buyer);
 !reactToSaleOffer(Product,Buyer,Price,false). // FIXME Initial is not actually false 

+!reactToSaleOffer(Product,Buyer,Price,Initial) : lastPrice(Product,Buyer,_) <-
 !respondToSaleOffer(Product,Buyer,Price).

+!reactToSaleOffer(Product,Buyer,Price,false) : not lastPrice(Product,Buyer,OldPrice) <-
 ?lastPrice(Product,Buyer,XYXYXY);
 .print("wtf",Product,Buyer,OldPrice).


+!respondToSaleOffer(Product,Buyer,Price) : findBestSale(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & offers(Product,MinPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & ((LastPrice - MinPrice)*StepFactor) >= MinStep
										  & (LastPrice - ((LastPrice - MinPrice)*StepFactor)) > Price <-
 .print("sale counterproposal",Price," ",LastPrice," ",MinPrice," ",LastPrice - ((LastPrice - MinPrice)*StepFactor));
 !makeSaleOffer(Product,Buyer,false).

+!respondToSaleOffer(Product,Buyer,Price) : findBestSale(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & offers(Product,MinPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & not (((LastPrice - MinPrice)*StepFactor) >= MinStep
										  & LastPrice - ((LastPrice - MinPrice)*StepFactor) > Price) 
										  & Price <= MinPrice <-
 .print("sale accept",Price," ",LastPrice," ",MinPrice," ",LastPrice - ((LastPrice - MinPrice)*StepFactor)).
 
+!respondToSaleOffer(Product,Buyer,Price) : findBestSale(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & offers(Product,MinPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & not (((LastPrice - MinPrice)*StepFactor) >= MinStep
										  & LastPrice - ((LastPrice - MinPrice)*StepFactor) > Price) 
										  & Price > MinPrice <-
 .print("sale reject",Price," ",LastPrice," ",MinPrice," ",LastPrice - ((LastPrice - MinPrice)*StepFactor)).
 
+!respondToSaleOffer(Product,Buyer,Price) : findBestSale(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & offers(Product,MinPrice) <- 
 .print("sale error ",Price," ",LastPrice," ",MinPrice," ",LastPrice - ((LastPrice - MinPrice)*StepFactor)).
 
+!addSale(Product,Buyer) : not sales(Product,Anything) <-
 +sales(Product,[Buyer]).
+!addSale(Product,Buyer) : sales(Product,OldSales) <-
 -sales(Product,OldSales);
 +sales(Product,[Buyer|OldSales]).
 
/* Plans for Buyer */

+requests(Product,Price) : true <- 
 .my_name(Me);
 .send(matchmaker,achieve,registerRequest(Product,Me));
 .send(matchmaker,achieve,getSellers(Product,Me)).

+!setSellers(Product,Sellers) : empty(Sellers) <- .print("No Sellers for now.").
+!setSellers(Product,Sellers) : not empty(Sellers) <- 
 +negotiations(Product,Sellers);
 !setupNegotiations(Product,Sellers).

+!setupNegotiations(Product,[]) : findBestNegotiation(Product,Best) & not (Best = null) <-
 !makeBuyOffer(Product,Best,true).
+!setupNegotiations(Product,[First|Rest]) : true <-
 ?requests(Product,Price);
 +lastPrice(Product,First,Price / 2);
 !setupNegotiations(Product,Rest).				

+!makeBuyOffer(Product,Seller,true) : not initialSent(Product,Seller) <-
 .my_name(Me);
 ?lastPrice(Product,Seller,OldPrice);
 ?requests(Product,MaxPrice);
 +initialSent(Product,Seller);
 +waitingFor(Product,Seller);
 .print("sends initial offer");
 .send(Seller,achieve,reactToSaleOffer(Product,Me,OldPrice,true)).
 
+!makeBuyOffer(Product,Seller,false) : true <-
 .my_name(Me);
 ?lastPrice(Product,Seller,OldPrice);
 ?requests(Product,MaxPrice);
 +initialSent(Product,Seller);
 +waitingFor(Product,Seller);
 ?stepFactor(StepFactor);
 -lastPrice(Product,Seller,OldPrice);
 +lastPrice(Product,Seller,OldPrice + ((MaxPrice-OldPrice)*StepFactor));
 .print("sends counteroffer",Me,Product,OldPrice + ((MaxPrice-OldPrice)*StepFactor));
 .send(Seller,achieve,reactToSaleOffer(Product,Me,OldPrice + ((MaxPrice-OldPrice)*StepFactor),false)).
 
+!reactToBuyOffer(Product,Seller,Price,true) : not lastPrice(Product,Seller,LastPrice) <-
 ?requests(Product,MaxPrice);
 +lastPrice(Product,Seller,MaxPrice / 2.0);
 !addNegotiation(Product,Seller);
 !reactToBuyOffer(Product,Seller,Price,false). // FIXME Initial is not actually false 
 
+!reactToBuyOffer(Product,Seller,Price,false) : lastPrice(Product,Seller,LastPrice)
                                              <-
 !respondToBuyOffer(Product,Seller,Price).
 
+!reactToBuyOffer(Product,Seller,Price,true) :  initialSent(Product,Seller) <-// FIXME Whatever
 .print("One message skipped").

+!respondToBuyOffer(Product,Seller,Price) : findBestNegotiation(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & requests(Product,MaxPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & ((MaxPrice-LastPrice)*StepFactor) >= MinStep
										  & (LastPrice + ((MaxPrice-LastPrice)*StepFactor)) < Price <-
 .print("buy counterproposal",Price," ",LastPrice," ",MaxPrice," ",LastPrice + ((MaxPrice-LastPrice)*StepFactor));
 !makeBuyOffer(Product,Seller,false).

+!respondToBuyOffer(Product,Seller,Price) : findBestNegotiation(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & requests(Product,MaxPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & not (((MaxPrice-LastPrice)*StepFactor) >= MinStep
										  & LastPrice + ((MaxPrice-LastPrice)*StepFactor) < Price) 
										  & Price <= MaxPrice <-
 .print("buy accept",Price," ",LastPrice," ",MaxPrice," ",LastPrice + ((MaxPrice-LastPrice)*StepFactor)).
 
+!respondToBuyOffer(Product,Seller,Price) : findBestNegotiation(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & requests(Product,MaxPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & not (((MaxPrice-LastPrice)*StepFactor) >= MinStep
										  & LastPrice + ((MaxPrice-LastPrice)*StepFactor) < Price)  
										  & Price > MaxPrice <-
 .print("buy reject",Price," ",LastPrice," ",MaxPrice," ",LastPrice + ((MaxPrice-LastPrice)*StepFactor)).
 
+!respondToBuyOffer(Product,Seller,Price) : findBestNegotiation(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & requests(Product,MaxPrice) <-
 .print("error: ",Price," ",LastPrice," ",MaxPrice," ",LastPrice + ((MaxPrice-LastPrice)*StepFactor)).
 
+!addNegotiation(Product,Seller) : not negotiations(Product,Anything) <-
 +negotiations(Product,[Seller]).
+!addNegotiation(Product,Seller) : negotiations(Product,OldNegotiations) <-
 -negotiations(Product,OldNegotiations);
 +negotiations(Product,[Seller|OldNegotiations]).
