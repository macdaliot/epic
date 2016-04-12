#!/bin/bash

for i in 3 5 7 10 15
do
   java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 vote $i train
done
