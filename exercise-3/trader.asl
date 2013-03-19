// Agent trader in project exercise3.mas2j

negoStep(0.2).

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
 !makeSaleOffer(Product,Best,true).
+!setupSales(Product,[First|Rest]) : true <-
 ?offers(Product,Price);
 +lastPrice(Product,First,Price * 2);
 !setupSales(Product,Rest).				

+!makeSaleOffer(Product,Buyer,Initial) : true <-
 .my_name(Me);
 ?lastPrice(Product,Buyer,OldPrice);
 ?offers(Product,MinPrice);
 +waitingFor(Product,Buyer);
 +initialSent(Product,Buyer);
 .send(Buyer,achieve,reactToBuyOffer(Product,Me,MinPrice,Initial)).
 
+!reactToSaleOffer(Product,Buyer,Price,true) : not lastPrice(Product,Buyer,LastPrice) <-
 ?offers(Product,MinPrice);
 +lastPrice(Product,Buyer,2 * MinPrice);
 !reactToSaleOffer(Product,Buyer,Price,false). // FIXME Initial is not actually false 
 
+!reactToSaleOffer(Product,Buyer,Price,false) : lastPrice(Product,Buyer,LastPrice)
                                              & (waitingFor(Product,Buyer) 
											    |waitingFor(Product,null) 
												|not waitingFor(Product,Anyone))<-
 !respondToSaleOffer(Product,Buyer,Price).

+!respondToSaleOffer(Product,Buyer,Price) : findBestSale(Product)
                                          & lastPrice(Product,Buyer,LastPrice)
										  & offers(Product,MinPrice)
										  & negoStep(Step)
										  & ((LastPrice - MinPrice)*Step) >= 0.1
										  & (LastPrice - ((LastPrice - MinPrice)*Step)) > Price <-
 .print("sale counterproposal").

+!respondToSaleOffer(Product,Buyer,Price) : findBestSale(Product)
                                          & lastPrice(Product,Buyer,LastPrice)
										  & offers(Product,MinPrice)
										  & negoStep(Step)
										  & (not((LastPrice - MinPrice)*Step) >= 0.1
										  & (LastPrice - ((LastPrice - MinPrice)*Step)) > Price) 
										  & Price <= MinPrice <-
 .print("sale accept").
 
+!respondToSaleOffer(Product,Buyer,Price) : findBestSale(Product)
                                          & lastPrice(Product,Buyer,LastPrice)
										  & offers(Product,MinPrice)
										  & negoStep(Step)
										  & (not((LastPrice - MinPrice)*Step) >= 0.1
										  & (LastPrice - ((LastPrice - MinPrice)*Step)) > Price) 
										  & Price > MinPrice <-
 .print("sale reject").
 
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
 !makeBuyOffer(Product,Best,true).
+!setupNegoriations(Product,[First|Rest]) : true <-
 ?requests(Product,Price);
 +lastPrice(Product,First,Price / 2);
 !setupNegotiations(Product,Rest).				

+!makeBuyOffer(Product,Seller,Initial) : true <-
 .my_name(Me);
 ?lastPrice(Product,Seller,OldPrice);
 ?requests(Product,MaxPrice);
 +waitingFor(Product,Seller);
 .send(Seller,achieve,reactToSaleOffer(Product,Me,MaxPrice,Initial)).
 
+!reactToBuyOffer(Product,Seller,Price,true) : not lastPrice(Product,Seller,LastPrice) <-
 ?requests(Product,MaxPrice);
 +lastPrice(Product,Seler,MaxPrice / 2.0);
 !reactToBuyOffer(Product,Seller,Price,false). // FIXME Initial is not actually false 
 
+!reactToBuyOffer(Product,Seller,Price,false) : lastPrice(Product,Seller,LastPrice)
                                              & (waitingFor(Product,Seller) 
											    |waitingFor(Product,null) 
												|not waitingFor(Product,Anyone))<-
 !respondToBuyOffer(Product,Seller,Price).

+!respondToBuyOffer(Product,Seller,Price) : findBestNegotiation(Product)
                                          & lastPrice(Product,Seller,LastPrice)
										  & requests(Product,MaxPrice)
										  & negoStep(Step)
										  & ((MaxPrice-LastPrice)*Step) >= 0.1
										  & (LastPrice + ((MaxPrice-LastPrice)*Step)) < Price <-
 .print("buy counterproposal").

+!respondToBuyOffer(Product,Seller,Price) : findBestSale(Product)
                                          & lastPrice(Product,Seller,LastPrice)
										  & requests(Product,MaxPrice)
										  & negoStep(Step)
										  & (not((MaxPrice-LastPrice)*Step) >= 0.1
										  & (LastPrice + ((MaxPrice-LastPrice)*Step)) < Price) 
										  & Price <= MaxPrice <-
 .print("buy accept").
 
+!respondToBuyOffer(Product,Seller,Price) : findBestSale(Product)
                                          & lastPrice(Product,Seller,LastPrice)
										  & requests(Product,MaxPrice)
										  & negoStep(Step)
										  & (not((MaxPrice-LastPrice)*Step) >= 0.1
										  & (LastPrice + ((MaxPrice-LastPrice)*Step)) < Price)  
										  & Price > MaxPrice <-
 .print("buy reject").

