#import pymongo 
import sys 
import os 
#from pymongo import MongoClient 
from moveBatch import moveBatch
rString = moveBatch([0.013939899509,0.552615213667,0.857404263741,0.904229890825,0.997930319545,0.407965088252,0.289232449629,0.457814305184,0.675406849476,0.884906175356],0.0,"twitter_train_labeled","twitter_train_unlabeled")
print str(rString)
