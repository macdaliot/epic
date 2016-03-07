import pymongo 
import sys 
import os 
from pymongo import MongoClient 
from moveBatch import moveBatch
rString = moveBatch([0.352619336951967,0.2378837898461611,0.6938306762138481,0.5497344619413898],0.0)
print str(rString)
