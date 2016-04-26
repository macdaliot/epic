#import pymongo 
import sys 
import os 
#from pymongo import MongoClient 
from moveBatch import moveBatch
from makeConllFromDBOutput import makeConll


pathToEpic = os.getcwd()
pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]

readFile = pathToEpic + "/data/ComparisonConllFiles/twitter_train_labeledStart.txt"
writeFile = pathToEpic + "/data/ComparisonConllFiles/twitter_train_labeledStart.conll"
makeConll(readFile, writeFile)
