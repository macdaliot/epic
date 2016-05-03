#import pymongo 
import sys 
import os 
#from pymongo import MongoClient 
from moveBatch import moveBatch
rString = moveBatch([0.909364392028,0.148396933955,0.159726324451,0.441706354691,0.31574890677,0.25480943832,0.220989030238,0.657345681623,0.396343343963,0.90789800256],0.0,"esp_train_conll2002_labeled","esp_train_conll2002_unlabeled")
print str(rString)
