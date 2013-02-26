#!/bin/bash

java -cp lib/jade.jar:bin/:. jade.Boot -gui -host localhost -agents "matchmaker:nl.uu.cs.map.jade.agent.MatchmakerAgent(arg1,arg2)"
