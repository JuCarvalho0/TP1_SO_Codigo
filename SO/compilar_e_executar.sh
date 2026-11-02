#!/bin/sh
# Compila e roda os testes. Requer JDK 11 ou mais novo.
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out ProdutorConsumidor.ProdCons
java -cp out trabalho.Main todos
java -cp out trabalho.TestesTodos
