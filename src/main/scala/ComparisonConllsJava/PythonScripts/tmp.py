#import pymongo 
import sys 
import os 
#from pymongo import MongoClient 
from moveBatch import moveBatch
rString = moveBatch([0.443901951885,0.621121102495,0.543740589632,0.991987234411,0.925296245954,0.437075573304,0.733128609552,0.128855697292,0.724956552888,0.301134676223],0.0,"train_conll2000_labeled","train_conll2000_unlabeled")
print str(rString)
