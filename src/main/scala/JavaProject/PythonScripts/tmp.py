import pymongo 
import sys 
import os 
from pymongo import MongoClient 
from moveBatch import moveBatch
rString = moveBatch([0.3070312723350612,0.12674844967256582,0.507281676517855,0.3746449381048782],0.0)
print str(rString)
