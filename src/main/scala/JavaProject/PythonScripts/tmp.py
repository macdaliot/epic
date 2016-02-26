import pymongo 
import sys 
import os 
from pymongo import MongoClient 
from moveBatch import moveBatch
from relabelBatch import relabeledBatch
rString = relabelBatch([0],0.010714285714285714)
print str(rString)
