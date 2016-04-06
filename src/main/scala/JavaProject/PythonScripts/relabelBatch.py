#import pymongo
import sys
import os
#from pymongo import MongoClient
from makeConllFromDBOutput import makeConll
from getJustSentences import getJustSentences

# python will convert \n to os.linesep
def relabelBatch(randomIds,noise):
	pathToEpic = os.getcwd()
	pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]
	returnString = "Tmp file: "
	print "Inside moveBatch"
	# Move Batch between databases
	client = MongoClient('mon-entity-event-r13-2.recfut.com:27016')
	db = client.rf_entity_curation
	labeled = db.malware_labeled
	unlabeled = db.malware_unlabeled
	batch = open(os.path.expanduser(pathToEpic + "/data/PoolData/batch.txt"),'w')
	readlabeled = open(os.path.expanduser(pathToEpic + "/data/PoolData/labeledPool.txt"), 'r')
	lines = readlabeled.readlines()
	readlabeled.close()
	print "Labeled openened for rewriting"
	#print "randomIds "  + str(randomIds)

	################## Batch moved  in database #############
	#for oneId in randomIds:
	#	tmpId = unlabeled.find({"random" : oneId})
	#	labeled.insert(tmpId)
	#	unlabeled.remove({"random" : oneId})
	#	tmpId = labeled.find({"random" : oneId})
	#	batch.write(str(tmpId[0]))
	#	batch.write("\n")

	#print "Starting to remove id from textfile"
	for line in lines:
		idFound = False
		for oneID in randomIds:
			if not (line.find(str(oneID)[0:len(str(oneID))-2])==-1):
				idFound = True
			#print str(idFound)+" " +str(oneID)[0:len(str(oneID))-2] +"\n"+line
		if idFound:
			batch.write(line)
			#print line + " does not include " +oneId
		#print str(idFound)+" " + +"\n"+line
		#returnString += str(idFound) + " " + line + "\n"


	batch.close()


	# Get Conll of the batches and add these to all conll's of labeled pool
	makeConll(pathToEpic + "/data/PoolData/batch.txt", pathToEpic + "/data/PoolData/batchConll.conll", noise)

	labeledOrig = open(os.path.expanduser(pathToEpic + "/data/PoolData/labeledPool.txt"), 'a')
	labeledOrigConll = open(os.path.expanduser(pathToEpic + "/data/PoolData/labeledPool.conll"),'a')

	batch = open(os.path.expanduser(pathToEpic + "/data/PoolData/batch.txt"),'r')
	batchConll = open(os.path.expanduser(pathToEpic + "/data/PoolData/batchConll.conll"),'r')

	labeledOrig.write(batch.read())
	labeledOrigConll.write(batchConll.read())
	labeledOrig.close()
	labeledOrigConll.close()

	batch.close()
	batchConll.close()

	#os.remove(os.path.expanduser("pathToEpic + "/data/batch.txt"))
	#os.remove(os.path.expanduser("pathToEpic + "/data/batchConll.conll"))





	return returnString

















