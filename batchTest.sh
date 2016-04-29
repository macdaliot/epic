#!/bin/bash

#for i in 0 0.25 0.5 1 2 4 100
#do
#   java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 lc infodens $i train
#done


#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 vote 5 noise train
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 infodens noise train
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 lc infodens 0.25 noise train



java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 gibbs noise train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 vote 5 noise train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 gibbs infodens 0.25 train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 vote 5 infodens 0.25 train



#Helgen
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar ComparisonConllsJava.Tester esp_train_conll2002_unlabeled esp_train_conll2002_labeled esp_test_conll2002 600 LC train
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar ComparisonConllsJava.Tester ned_train_conll2002_unlabeled ned_train_conll2002_labeled ned_test_conll2002 600 LC train
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar ComparisonConllsJava.Tester train_conll2000_unlabeled train_conll2000_labeled test_conll2000.conll 600 LC train
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar ComparisonConllsJava.Tester twitter_train_unlabeled twitter_train_labeled twitter_test 600 LC train





#for i in $(seq 400 200 2000)
#do
#   java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester lc $i train
#done