#import pymongo
import sys
import os
##from pymongo import MongoClient
from makeConllFromDBOutput import makeConll
#from getJustSentences import getJustSentences

# python will convert \n to os.linesep
def moveBatch(randomIds,noise, labeledFile,unlabeledFile):
	pathToEpic = os.getcwd()
	pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]
	returnString = "Tmp file: "
	print "Inside moveBatch"
	batch = open(os.path.expanduser(pathToEpic + "/data/ComparisonConllFiles/batch.txt"),'w')
	readUnlabeled = open(os.path.expanduser(pathToEpic + "/data/ComparisonConllFiles/"+unlabeledFile+".conll"), 'r')
	lines = readUnlabeled.readlines()
	readUnlabeled.close()
	writeUnlabeled = open(os.path.expanduser(pathToEpic + "/data/ComparisonConllFiles/"+unlabeledFile+".txt"), 'w')
	print "Unlabeled openened for writing"
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
		if not idFound:
			#print "Write \""+line+"\" to unlabeled"
			writeUnlabeled.write(line)
		else:
			#print "Write \""+line+"\" to batch"
			batch.write(line)
			#print line + " does not include " +oneId
		#print str(idFound)+" " + +"\n"+line
		#returnString += str(idFound) + " " + line + "\n"

	writeUnlabeled.close()
	batch.close()


	# Get Conll of the batches and add these to all conll's of labeled pool
	makeConll(pathToEpic + "/data/ComparisonConllFiles/batch.txt", pathToEpic + "/data/ComparisonConllFiles/batchConll.conll")

	labeledOrig = open(os.path.expanduser(pathToEpic + "/data/ComparisonConllFiles/"+labeledFile+"txt"), 'a')
	labeledOrigConll = open(os.path.expanduser(pathToEpic + "/data/ComparisonConllFiles/"+labeledFile+".conll"),'a')

	batch = open(os.path.expanduser(pathToEpic + "/data/ComparisonConllFiles/batch.txt"),'r')
	batchConll = open(os.path.expanduser(pathToEpic + "/data/ComparisonConllFiles/batchConll.conll"),'r')

	labeledOrig.write(batch.read())
	labeledOrigConll.write(batchConll.read())
	labeledOrig.close()
	labeledOrigConll.close()

	batch.close()
	batchConll.close()

	#os.remove(os.path.expanduser(pathToEpic + "/data/PoolData/batch.txt"))
	#os.remove(os.path.expanduser(pathToEpic + "/data/PoolData/batchConll.conll"))





	return returnString

















