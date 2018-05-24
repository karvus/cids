# Wireless Sensor Network Simulation

This folder contains the source code for the simulation network.  
It was originally created using Netbeans, but should be runnable in any 
Java IDE.

## Overview

The simulation works as follows:

1. A main file (`Simulation.java`) creates a `Network` and adds `Node`s 
to it. This also handles the network topology.
2. The main file repeatedly calls `Network.simulate()`:
    - The Network increments the global "real" time
    - The Network delivers any queued up messages whose arrival time has 
come
    - The Network asks each Node to simulate
        - The Node may send new messages by calling 
`Network.sendMessage()`. 
These will be delivered after a configurable delay.

Overall, the main algorithm work should happen in the implementation of 
`Node.simulate()` and `Node.receiveMessage()`.

## Working with the project

As all of the algorithm work should happen in the `Node` implementation, 
most work will be of the form:

1. Create an algorithm-specific folder (e.g. `/MMTS`)
1. Create a new `Node` implementation in said folder
2. Implement `.simulate()` and `.receiveMessage()`
    - Create new `Message` implementations as needed

A custom logger has been implemented, which logs out data with a prefix 
that contains the current Network time. This is useful for gaining an 
easy overview of when each message arrives.  
Use it by creating a new `WSNLogger`, and then calling `.log()` on it 
(or one of the level-specific proxy methods). For logging data to a file, use `.write()` -  this is useful for gathering data for later analysis.
