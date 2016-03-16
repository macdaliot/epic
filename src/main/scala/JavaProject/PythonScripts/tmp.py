import pymongo 
import sys 
import os 
from pymongo import MongoClient 
from moveBatch import moveBatch
rString = moveBatch([0.2780673430247228,0.78774782931376,0.4432866838145323,0.3121159447045604,0.05413063352843228],0.0)
print str(rString)
