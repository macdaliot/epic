#!/bin/bash

java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 LC infodens 0.25 noise 0.2 train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 infodens train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 gibbs infodens 0.25 train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 vote 5 infodens 0.25 train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 gibbs train
java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester 600 vote train



#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar epic.sequences.SemiConllNerPipeline --train data/ComparisonConllFiles/train_conll2000_labeled.conll --test data/ComparisonConllFiles/test_conll2000.conll --modelOut data/our_malware.ser.gz
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar ComparisonConllsJava.Tester train_conll2000_unlabeled train_conll2000_labeled test_conll2000 10 LC train
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar epic.sequences.SemiConllNerPipeline --train data/ComparisonConllFiles/train_conll2000_labeled.conll --test data/ComparisonConllFiles/test_conll2000.conll --modelOut data/our_malware.ser.gz
#java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar ComparisonConllsJava.Tester train_conll2000_unlabeled train_conll2000_labeled test_conll2000 10 random train


