import pymongo
import sys
import os
from pymongo import MongoClient
from makeConllFromDBOutput import makeConll
from getJustSentences import getJustSentences

# python will convert \n to os.linesep
def moveBatch(randomIds):

	# Move Batch between databases
	client = MongoClient('mon-entity-event-r13-2.recfut.com:27016')
	db = client.rf_entity_curation
	labeled = db.malware_labeled
	unlabeled = db.malware_unlabeled
	f = open(os.path.expanduser("~//epic/epic/data/batch.txt"),'w')
	readUnlabeled = open(os.path.expanduser("~/epic/epic/data/unlabeledPool.txt"), 'r')
	lines = readUnlabeled.readlines()
	readUnlabeled.close()
	writeUnlabeled = open(os.path.expanduser("~/epic/epic/data/unlabeledPool.txt"), 'w')
	print "randomIds "  + str(randomIds)

	for oneId in randomIds:
		tmpId = unlabeled.find({"random" : oneId})
		labeled.insert(tmpId)
		unlabeled.remove({"random" : oneId})
		tmpId = labeled.find({"random" : oneId})
		f.write(str(tmpId[0]))
		f.write("\n")

	print "Starting to remove id from textfile"
	for line in lines:
		idFound = False
		for oneID in randomIds:
			if line.find(str(oneID)):
				idFound = True
		if not idFound:
			writeUnlabeled.write(line)
			print line + " does not include " +oneId

	writeUnlabeled.close()
	f.close()


	# Get Conll of the batches and add these to all conll's of labeled pool
	makeConll("~/epic/epic/data/batch.txt", "~/epic/epic/data/batchConll.conll")

	labeledOrig = open(os.path.expanduser("~/epic/epic/data/labeledPool.txt"), 'a')
	labeledOrigConll = open(os.path.expanduser("~/epic/epic/data/labeledPool.conll"),'a')

	batch = open(os.path.expanduser("~/epic/epic/data/batch.txt"),'r')
	batchConll = open(os.path.expanduser("~/epic/epic/data/batchConll.conll"),'r')

	labeledOrig.write(batch.read())
	labeledOrigConll.write(batchConll.read())

	os.remove(os.path.expanduser("~/epic/epic/data/batch.txt"))
	os.remove(os.path.expanduser("~/epic/epic/data/batchConll.conll"))


















