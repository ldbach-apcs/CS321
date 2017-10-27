#!/bin/bash
mkdir bin
javac -d bin SimpleCalculator.java
java -cp bin SimpleCalculator test.e && echo ""
