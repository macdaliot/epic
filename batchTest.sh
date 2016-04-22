#!/bin/bash

#for i in 3 5 7 10
#do
#   java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 vote $i train
#done




#for i in $(seq 100 100 600)
#do
#   java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester threshold $i train
#done

#Helgen
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 lc infodens train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 vote 5 infodens train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 gibbs infodens train


for i in 0.25 0.5 1 2 4
do
   java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 lc infodens $i train
done


#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 lc noise train

#for i in $(seq 400 200 2000)
#do
#   java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester lc $i train
#done