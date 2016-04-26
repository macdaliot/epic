#import pymongo 
import sys 
import os 
#from pymongo import MongoClient 
from moveBatch import moveBatch
from makeConllFromDBOutput import makeConll


pathToEpic = os.getcwd()
pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]

readFile = pathToEpic + "/data/ComparisonConllFiles/ned_train_conll2002_labeledStart.txt"
writeFile = pathToEpic + "/data/ComparisonConllFiles/ned_train_conll2002_labeledStart.conll"
makeConll(readFile, writeFile)
