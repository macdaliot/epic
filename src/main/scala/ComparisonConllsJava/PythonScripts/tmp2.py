#import pymongo 
import sys 
import os 
#from pymongo import MongoClient 
from moveBatch import moveBatch
from makeConllFromDBOutput import makeConll


pathToEpic = os.getcwd()
pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]

readFile = pathToEpic + "/data/PoolData/unlabeledPoolStart.txt"
writeFile = pathToEpic + "/data/PoolData/unlabeledPoolStart.conll"
makeConll(readFile, writeFile,0.0)
