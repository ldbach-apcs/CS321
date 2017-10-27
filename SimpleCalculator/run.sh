#!/bin/bash
javac -d bin SimpleCalculator.java
java -cp bin SimpleCalculator example1.e && echo ""
java -jar SimpleCalculator.jar example1.e && echo ""
