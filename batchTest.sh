#!/bin/bash

for i in $(seq 200 200 1600)
do
   java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester $i LC train 
done


