// Agent trader in project exercise3.mas2j

/*

lastPrice(Product,Partner,Price) : The last price offered in the negotiation for
Product with Partner.

waitingFor(Product,Partner) : This trader is waiting for a message from Partner 
in the negotiation about Product and may not send messages to others until it
gets a response from Partner. If not waiting, Partner is null.

respondTo(Product,List) : List of partners who expect a response concerning 
Product.

initialSent(Product,Partner) : This trader has sent an initial offer to Partner
concerning Product. If this trader wants to buy Product, it will ignore incoming 
initial offers from Partner concerning Product to avoid having two negotiations 
about the same Product at once.

sales(Product,List) : List of partners to who want Product.

negotiations(Product,List) : List of partners who offer Product.

offers(Product,MinPrice) : Indicates that this trader wants to sell Product for
at least MinPrice. 

requests(Product,MaxPrice) : Indicates that this trader wants to buy Product for
at most MaxPrice. 

sold(Product) : This Product has been sold, requests concerning it will be
rejected.

bought(Product) : As above.

*/


/* Constants */

stepFactor(0.2).
minStep(0.1).

/* Rules */

empty([]).


// finds the sale with the highest lastPrice for the given product
// (for seller)
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
 
 
// same as above, but finds the negotiation with lowest lastPrice
// (for buyer)
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

 
// Removes atom arg1 from list arg0, result in arg2
removeFromList([],_,[]).
 
removeFromList([Partner|Rest],Partner,Result) :-
 removeFromList(Rest,Partner,Result).
 
removeFromList([Someone|Rest],Partner,[Someone|Result]) :-
 removeFromList(Rest,Partner,Result).

 
/* Common plans */

@addRespondToNew[atomic]
+!addRespondTo(Product,Partner)  : not respondTo(Product,Anyone) <-
 .print("added RespondTo",Product,Partner);
 +respondTo(Product,[Partner]).
 
@addRespondToExisting[atomic]
+!addRespondTo(Product,Partner)  : respondTo(Product,Current) <-
 .print("added RespondTo",Product,Partner);
 -respondTo(Product,Current);
 +respondTo(Product,[Partner|Current]).
 
@removeRespondToEmpty[atomic]
+!removeRespondTo(Product,Partner)  : not respondTo(Product,_) <-
 +respondTo(Product,[]).

@removeRespondToNonEmpty[atomic]
+!removeRespondTo(Product,Partner)  : respondTo(Product,List) <-
 ?removeFromList(List,Partner,NewList);
 -respondTo(Product,List);
 +respondTo(Product,NewList).

/* Plans for Seller */

@offers[atomic]
+offers(Product,Price)  : true <- 
 .my_name(Me);
 +waitingFor(Product,null);
 .send(matchmaker,achieve,registerOffer(Product,Me));
 .send(matchmaker,achieve,getBuyers(Product,Me)).

@setBuyersEmpty[atomic]
+!setBuyers(Product,Buyers)  : empty(Buyers) <- .print("No Buyers for now for ",Product).

@setBuyersNonEmpty[atomic]
+!setBuyers(Product,Buyers)  : not empty(Buyers) <- 
 .print("Got buyers for ",Product,":",Buyers);
 +sales(Product,Buyers);
 !setupSales(Product,Buyers).

@setupSalesNonEmpty[atomic]
+!setupSales(Product,[First|Rest])  : true <-
 ?offers(Product,Price);
 +lastPrice(Product,First,Price * 2);
 !setupSales(Product,Rest).

@setupSalesEmpty[atomic]
+!setupSales(Product,[])  : true <-
 ?sales(Product,TestBuyers);
 ?findBestSale(Product,Best);
 !makeSaleOffer(Product,Best,true).			

@makeSaleOfferInitialNotSent[atomic]
+!makeSaleOffer(Product,Buyer,true)  : lastPrice(Product,Buyer,_) & not initialSent(Product,Buyer) <-
 ?waitingFor(Product,Current);
 -waitingFor(Product,Current);
 +waitingFor(Product,Buyer);
 .my_name(Me);
 ?lastPrice(Product,Buyer,OldPrice);
 ?offers(Product,MinPrice);
 +initialSent(Product,Buyer);
 .print("Offering ",Buyer," to sell ",Product," for ",OldPrice);
 !removeRespondTo(Product,Buyer);
 .send(Buyer,achieve,reactToBuyOffer(Product,Me,OldPrice,true)).
 
@makeSaleOfferInitialSent[atomic]
+!makeSaleOffer(Product,Buyer,true)  : lastPrice(Product,Buyer,_) & initialSent(Product,Buyer) <-
 .print("TODO").
 
@makeSaleOfferWaitingForOther[atomic]
+!makeSaleOffer(Product,Buyer,false)  : not (waitingFor(Product,Buyer) | waitingFor(Product,null)) <-
 !addRespondTo(Product,Buyer).
 
@makeSaleOfferStandard[atomic]
+!makeSaleOffer(Product,Buyer,false)  : true <-
 ?waitingFor(Product,Current);
 -waitingFor(Product,Current);
 +waitingFor(Product,Buyer);
 .my_name(Me);
 ?lastPrice(Product,Buyer,OldPrice);
 ?offers(Product,MinPrice);
 +initialSent(Product,Buyer);
 ?stepFactor(StepFactor);
 -lastPrice(Product,Buyer,OldPrice);
 +lastPrice(Product,Buyer,OldPrice - ((OldPrice-MinPrice)*StepFactor));
 .print("Offering ",Buyer," to sell ",Product," for ",OldPrice - ((OldPrice-MinPrice)*StepFactor));
 !removeRespondTo(Product,Buyer);
 .send(Buyer,achieve,reactToBuyOffer(Product,Me,OldPrice - ((OldPrice-MinPrice)*StepFactor),false)).
 
@reactToSaleSold[atomic]
+!reactToSaleOffer(Product,Buyer,_,_) : sold(Product) <-
 !rejectSaleOffer(Product,Buyer).
 
@reactSaleUnknownPartner[atomic]
+!reactToSaleOffer(Product,Buyer,Price,true)  : not lastPrice(Product,Buyer,LastPrice) <-
 ?offers(Product,MinPrice);
 +lastPrice(Product,Buyer,2 * MinPrice);
 !addSale(Product,Buyer);
 !reactToSaleOffer(Product,Buyer,Price,false).
 
@reactSaleWaitingForOther[atomic]
+!reactToSaleOffer(Product,Buyer,Price,Initial)  : not (waitingFor(Product,Buyer) | waitingFor(Product,null)) <-
 !addRespondTo(Product,Buyer).

@reactSaleStandard[atomic]
+!reactToSaleOffer(Product,Buyer,Price,Initial)  : lastPrice(Product,Buyer,_) <-
 !respondToSaleOffer(Product,Buyer,Price).
 
@respondSaleWaitingForOther[atomic]
+!respondToSaleOffer(Product,Buyer,Price)  : not ( waitingFor(Product,Buyer) | waitingFor(Product,null)) <-
 !addRespondTo(Product,Buyer).

@respondSaleCounter[atomic]
+!respondToSaleOffer(Product,Buyer,Price)  : findBestSale(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & offers(Product,MinPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & ((LastPrice - MinPrice)*StepFactor) >= MinStep
										  & (LastPrice - ((LastPrice - MinPrice)*StepFactor)) > Price <-
 !makeSaleOffer(Product,Buyer,false).

@respondSaleAccept[atomic]
+!respondToSaleOffer(Product,Buyer,Price)  : findBestSale(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & offers(Product,MinPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & not (((LastPrice - MinPrice)*StepFactor) >= MinStep
										  & LastPrice - ((LastPrice - MinPrice)*StepFactor) > Price) 
										  & Price >= MinPrice <-
 .print("Accepting offer from ",Buyer," to sell ",Product," for ",Price);
 ?.my_name(Me);
 -waitingFor(Product,Buyer);
 +waitingFor(Product,null);
 !removeRespondTo(Product,Buyer);
 ?respondTo(Product,List);
 !removeOffer(Product,List);
 +sold(Product);
 .send(Buyer,achieve,handleAcceptBuy(Product,Me,Price)).
 
@respondSaleReject[atomic]
+!respondToSaleOffer(Product,Buyer,Price)  : findBestSale(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & offers(Product,MinPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & not (((LastPrice - MinPrice)*StepFactor) >= MinStep
										  & LastPrice - ((LastPrice - MinPrice)*StepFactor) > Price) 
										  & Price < MinPrice <-
 .print("Rejecting offer from ",Buyer," to sell ",Product," for ",Price);
 !rejectSaleOffer(Product,Buyer);
 !initiateNewRoundSale(Product).
 
@addSaleEmpty[atomic]
+!addSale(Product,Buyer)  : not sales(Product,Anything) <-
 +sales(Product,[Buyer]).
 
@addSaleNonEmpty[atomic]
+!addSale(Product,Buyer)  : sales(Product,OldSales) <-
 -sales(Product,OldSales);
 +sales(Product,[Buyer|OldSales]).
 
@removeOfferEmpty[atomic]
+!removeOffer(Product,[]) : true <-
 -offers(Product,Price).

@removeOfferNonEmpty[atomic]
+!removeOffer(Product,[First|Rest]) : true <-
 !rejectSaleOffer(Product,First);
 !removeOffer(Product,Rest).

@rejectSaleOffer[atomic]
+!rejectSaleOffer(Product,Buyer) : true <-
 ?.my_name(Me);
 !removeRespondTo(Product,Buyer);
 -lastPrice(Product,Buyer,_);
 ?sales(Product,Buyers);
 ?removeFromList(Buyers,Buyer,NewList);
 -sales(Product,Buyers);
 +sales(Product,NewList);
 .send(Buyer,achieve,handleRejectBuy(Product,Me)).
 
@intiateSaleEmpty[atomic]
+!initiateNewRoundSale(Product) : findBestSale(Product,null) <-
 .print("Currently no other buyers for ",Product).
 
@initiateSaleNonInitial[atomic]
+!initiateNewRoundSale(Product) : findBestSale(Product,Best) & initialSent(Product,Best) <-
 !makeSaleOffer(Product,Best,false).
 
@initiateSaleInitial[atomic]
+!initiateNewRoundSale(Product) : findBestSale(Product,Best) & not initialSent(Product,Best) <-
 !makeSaleOffer(Product,Best,true).
 
@handleAcceptSale[atomic]
+!handleAcceptSale(Product,Buyer,Price) : true <-
 .print("Sold ",Product," to ",Buyer," for ",Price);
 -waitingFor(Product,Buyer);
 +sold(Product);
 !removeRespondTo(Product,Buyer);
 ?respondTo(Product,List);
 !removeOffer(Product,List).

@handleRejectSale[atomic]
+!handleRejectSale(Product,Buyer) : true <-
 !removeRespondTo(Product,Buyer);
 -lastPrice(Product,Buyer,_);
 ?sales(Product,Buyers);
 ?removeFromList(Buyers,Buyer,NewList);
 -sales(Product,Buyers);
 +sales(Product,NewList);
 .print("Got reject for ",Product," from ",Buyer);
 !initiateNewRoundSale(Product).
 
 
/* Plans for Buyer */

@requests[atomic]
+requests(Product,Price)  : true <- 
 .my_name(Me);
 +waitingFor(Product,null);
 .send(matchmaker,achieve,registerRequest(Product,Me));
 .send(matchmaker,achieve,getSellers(Product,Me)).

@setSellersEmpty[atomic]
+!setSellers(Product,Sellers)  : empty(Sellers) <- .print("No Sellers for now for ",Product).

@setSellersNonEmpty[atomic]
+!setSellers(Product,Sellers)  : not empty(Sellers) <- 
 +negotiations(Product,Sellers);
 !setupNegotiations(Product,Sellers).

@setupNegosEmpty[atomic]
+!setupNegotiations(Product,[])  : findBestNegotiation(Product,Best) & not (Best = null) <-
 !makeBuyOffer(Product,Best,true).
 
@setupNegosNonEmpty[atomic]
+!setupNegotiations(Product,[First|Rest])  : true <-
 ?requests(Product,Price);
 +lastPrice(Product,First,Price / 2);
 !setupNegotiations(Product,Rest).				

@makeBuyOfferInitialNotSent[atomic]
+!makeBuyOffer(Product,Seller,true)  : lastPrice(Product,Seller,_) & not initialSent(Product,Seller) <-
 +initialSent(Product,Seller);
 ?waitingFor(Product,Current);
 -waitingFor(Product,Current);
 +waitingFor(Product,Seller);
 .my_name(Me);
 ?lastPrice(Product,Seller,OldPrice);
 ?requests(Product,MaxPrice);
 .print("Offering ",Seller," to buy ",Product," for ",OldPrice);
 !removeRespondTo(Product,Seller);
 .send(Seller,achieve,reactToSaleOffer(Product,Me,OldPrice,true)).
 
@makeBuyOfferInitialSent[atomic]
+!makeBuyOffer(Product,Seller,true)  : initialSent(Product,Seller) <-
 .print("TODO").
 
@reactToBuySold[atomic]
+!reactToBuyOffer(Product,Seller,_,_) : bought(Product) <-
 !rejectBuyOffer(Product,Seller).
 
@reactBuySkip[atomic]
+!reactToBuyOffer(Product,Seller,Price,true)  :  initialSent(Product,Seller) <-
 .print("Ignored offer for ",Product," from ",Seller). 

@reactBuyUnknownPartner[atomic]
+!reactToBuyOffer(Product,Seller,Price,true)  : not initialSent(Product,Seller) & not lastPrice(Product,Seller,LastPrice) <-
 ?requests(Product,MaxPrice);
 +lastPrice(Product,Seller,MaxPrice / 2.0);
 !addNegotiation(Product,Seller);
 !reactToBuyOffer(Product,Seller,Price,true). 
 
@reactBuyWaitingForOther[atomic]
+!reactToBuyOffer(Product,Seller,Price,Initial)  : not (waitingFor(Product,Seller) | waitingFor(Product,null)) <-
 !addRespondTo(Product,Seller).
 
@reactBuyStandard[atomic]
+!reactToBuyOffer(Product,Seller,Price,Initial)  : lastPrice(Product,Seller,LastPrice) <-
 !respondToBuyOffer(Product,Seller,Price,Initial).
 
@respondBuyWaitingForOther[atomic]
+!respondToBuyOffer(Product,Seller,Price,Initial)  : not (waitingFor(Product,Seller) | waitingFor(Product,null)) <-
 !addRespondTo(Product,Seller).
 
@respondBuySkip[atomic]
+!respondToBuyOffer(Product,Seller,_,true)  : initialSent(Product,Seller) <-
 .print("Ignored offer for ",Product," from ",Seller). 

@respondBuyCounter[atomic]
+!respondToBuyOffer(Product,Seller,Price,Initial)  : findBestNegotiation(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & requests(Product,MaxPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & ((MaxPrice-LastPrice)*StepFactor) >= MinStep
										  & (LastPrice + ((MaxPrice-LastPrice)*StepFactor)) < Price <-
 +initialSent(Product,Seller);
 ?waitingFor(Product,Current);
 -waitingFor(Product,Current);
 +waitingFor(Product,Seller);
 .my_name(Me);
 ?lastPrice(Product,Seller,OldPrice);
 ?requests(Product,MaxPrice);
 ?stepFactor(StepFactor);
 -lastPrice(Product,Seller,OldPrice);
 +lastPrice(Product,Seller,OldPrice + ((MaxPrice-OldPrice)*StepFactor));
 .print("Offering ",Seller," to buy ",Product," for ",OldPrice + ((MaxPrice-OldPrice)*StepFactor));
 !removeRespondTo(Product,Seller);
 .send(Seller,achieve,reactToSaleOffer(Product,Me,OldPrice + ((MaxPrice-OldPrice)*StepFactor),false)).

@respondBuyAccept[atomic]
+!respondToBuyOffer(Product,Seller,Price,Initial)  : findBestNegotiation(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & requests(Product,MaxPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & not (((MaxPrice-LastPrice)*StepFactor) >= MinStep
										  & LastPrice + ((MaxPrice-LastPrice)*StepFactor) < Price) 
										  & Price <= MaxPrice <-
 .print("Accepting offer from ",Seller," to buy ",Product," for ",Price);
 ?.my_name(Me);
 -waitingFor(Product,Seller);
 +waitingFor(Product,null);
 !removeRespondTo(Product,Seller);
 ?respondTo(Product,List);
 !removeRequest(Product,List);
 +bought(Product);
 .send(Seller,achieve,handleAcceptSale(Product,Me,Price)).
 
@respondBuyReject[atomic]
+!respondToBuyOffer(Product,Seller,Price,Initial)  : findBestNegotiation(Product,Best)
                                          & lastPrice(Product,Best,LastPrice)
										  & requests(Product,MaxPrice)
										  & stepFactor(StepFactor)
										  & minStep(MinStep)
										  & not (((MaxPrice-LastPrice)*StepFactor) >= MinStep
										  & LastPrice + ((MaxPrice-LastPrice)*StepFactor) < Price)  
										  & Price > MaxPrice <-
 .print("Rejecting offer from ",Seller," to buy ",Product," to buy ",LastPrice + ((MaxPrice-LastPrice)*StepFactor));
 !rejectBuyOffer(Product,Seller);
 !initiateNewRoundBuy(Product).
 
@addNegosEmpty[atomic]
+!addNegotiation(Product,Seller)  : not negotiations(Product,Anything) <-
 +negotiations(Product,[Seller]).
 
@addNegosNonEmpty[atomic]
+!addNegotiation(Product,Seller)  : negotiations(Product,OldNegotiations) <-
 -negotiations(Product,OldNegotiations);
 +negotiations(Product,[Seller|OldNegotiations]).
 
@removeRequestEmpty[atomic]
+!removeRequest(Product,[]) : true <-
 -requests(Product,Price).

@removeRequestNonEmpty[atomic]
+!removeRequest(Product,[First|Rest]) : true <-
 !rejectBuyOffer(Product,First);
 !removeRequest(Product,Rest).

@rejectBuyOffer[atomic]
+!rejectBuyOffer(Product,Seller) : true <-
 ?.my_name(Me);
 !removeRespondTo(Product,Seller);
 -lastPrice(Product,Seller,_);
 ?negotiations(Product,Sellers);
 ?removeFromList(Sellers,Seller,NewList);
 -negotiations(Product,Sellers);
 +negotiations(Product,NewList);
 .send(Seller,achieve,handleRejectSale(Product,Me)).
 
@intiateBuyEmpty[atomic]
+!initiateNewRoundBuy(Product) : findBestNegotiation(Product,null) <-
 .print("Currently no other buyers for ",Product).
 
@initiateBuyNonInitial[atomic]
+!initiateNewRoundBuy(Product) : findBestNegotiation(Product,Best) & initialSent(Product,Best) <-
 !makeBuyOffer(Product,Best,false).
 
@initiateBuyInitial[atomic]
+!initiateNewRoundBuy(Product) : findBestNegotiation(Product,Best) & not initialSent(Product,Best) <-
 !makeBuyOffer(Product,Best,true).
 
@handleAcceptBuy[atomic]
+!handleAcceptBuy(Product,Seller,Price) : true <-
 .print("Bought ",Product," from ",Seller," for ",Price);
 -waitingFor(Product,Seller);
 +bought(Product);
 !removeRespondTo(Product,Seller);
 ?respondTo(Product,List);
 !removeRequest(Product,List).

@handleRejectBuy[atomic]
+!handleRejectBuy(Product,Seller) : true <-
 .print("Got reject for ",Product," from ",Seller);
 !removeRespondTo(Product,Seller);
 -lastPrice(Product,Seller,_);
 ?negotiations(Product,Sellers);
 ?removeFromList(Sellers,Seller,NewList);
 -negotiations(Product,Buyers);
 +negotiations(Product,NewList);
 !initiateNewRoundBuy(Product).
