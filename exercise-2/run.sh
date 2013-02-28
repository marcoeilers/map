#!/bin/bash

java -cp lib/jade.jar:bin/:. jade.Boot -gui -host localhost -agents "matchmaker:nl.uu.cs.map.jade.agent.MatchmakerAgent;trader:nl.uu.cs.map.jade.agent.TraderAgent(resource/trader.properties);buyer:nl.uu.cs.map.jade.agent.TraderAgent(resource/buyer.properties);seller:nl.uu.cs.map.jade.agent.TraderAgent(resource/seller.properties)"
