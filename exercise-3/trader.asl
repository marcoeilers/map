// Agent trader in project exercise3.mas2j

/* Initial beliefs and rules */

/* Initial goals */
offers(rabbit,20).

tralala(A,70) :- offers(A,20).


/* Plans */

// +!start : true <- .print("hello world."); env_add_offer("123", 35, "color", "red").

+offers(Product,Price) : true <- .my_name(Me);
                                 .send(matchmaker,achieve,getBuyers(A,Me)).
+!setBuyers(Product,[]) : true <-   ?tralala(A,B);
									.print(B).

+!setBuyers(Product,[First|Rest]) : true <- 
								  ?offers(Product,Price);
                                  +lastPrice(Product,First,Price * 2);
                                  !setBuyers(Product,Rest).
