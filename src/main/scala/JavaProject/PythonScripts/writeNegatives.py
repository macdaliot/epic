import pymongo
import sys
import os
from pymongo import MongoClient
from makeConllFromDBOutput import makeConll
#from getJustSentences import getJustSentences

pathToEpic = os.path.abspath(os.path.join(os.path.dirname(os.getcwd()),"../../../.."))

client = MongoClient('mon-entity-event-r13-6.recfut.com:27019')
db = client.rf_entity_curation
allMalware = db.malware_negatives

negatives = allMalware.find()
negativeFile = open(os.path.expanduser(pathToEpic+"/data/APInegatives.txt"),'w')

counter = 0
for i in negatives:
	negativeFile.write(str(i)+ "\n")
	print "counter " +str(counter)
	counter += 1


makeConll(pathToEpic+"/data/APInegatives.txt",pathToEpic+"/data/APInegatives.conll",0.0)
