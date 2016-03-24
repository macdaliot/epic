import pymongo
import sys
import os
from pymongo import MongoClient
from makeConllFromDBOutput import makeConll
#from getJustSentences import getJustSentences



client = MongoClient('mon-entity-event-r13-6.recfut.com:27019')
db = client.rf_entity_curation
allMalware = db.malware_negatives

negatives = allMalware.find()
negativeFile = open(os.path.expanduser("~/epic/epic/data/APInegatives.txt"),'w')

counter = 0
for i in negatives:
	negativeFile.write(str(i)+ "\n")
	print "counter " +str(counter)
	counter += 1


makeConll("~/epic/epic/data/APInegatives.txt","~/epic/epic/data/APInegatives.conll",0.0)
