#!/bin/bash

java -cp lib/jade.jar:bin/:. jade.Boot -gui -host localhost -agents "matchmaker:nl.uu.cs.map.jade.agent.MatchmakerAgent;trader:nl.uu.cs.map.jade.agent.TraderAgent(resource/trader.properties,1000);buyer:nl.uu.cs.map.jade.agent.TraderAgent(resource/buyer.properties,2000);seller:nl.uu.cs.map.jade.agent.TraderAgent(resource/seller.properties,3000)"
rm APDescription.txt
rm MTPs-Main-Container.txt
