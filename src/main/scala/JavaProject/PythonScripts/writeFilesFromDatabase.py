import pymongo
import sys
import os
from pymongo import MongoClient
from makeConllFromDBOutput import makeConll
#from getJustSentences import getJustSentences

# python will convert \n to os.linesep

#Set size of training and test sets
sizeOfTraining = float(sys.argv[1])
if len(sys.argv) < 3:
	endSet = 1
else:
	endSet = float(sys.argv[2])

print sizeOfTraining,endSet


client = MongoClient('mon-entity-event-r13-2.recfut.com:27016')
db = client.rf_entity_curation
allMalware = db.malware_copy
######################
# Open the databases we need to drop before making new ones.
testMalware = db.malware_test
bigPool = db.malware_training
startPool = db.malware_labeled
unlabeledPool = db.malware_unlabeled
testMalware.remove({})
bigPool.remove({})
startPool.remove({})
unlabeledPool.remove({})

########################
#Create databases to hold test and training sets.
#db.create_collection("malware_test")
#db.create_collection("malware_training")
#testMalware = db.malware_test
#bigPool = db.malware_training
trainingCollection = allMalware.find({"random" : { "$gt": 0, "$lt": sizeOfTraining }})
testCollection = allMalware.find({"random" : { "$gt": sizeOfTraining, "$lt": endSet }})
bigPool.insert(trainingCollection)
testMalware.insert(testCollection)



########################
# Divide the training set into a small labeled pool for the model to start on
# and a bigger unlabeled pool which we will train the model with.
sizeOfstartPool = 0.05*sizeOfTraining
tmpStartPool = bigPool.find({"random" : { "$gt": 0, "$lt": sizeOfstartPool}})
tmpUnlabeledPool = bigPool.find({"random" : { "$gt": sizeOfstartPool, "$lt": 1 }})
startPool = db.malware_labeled
unlabeledPool = db.malware_unlabeled
startPool.insert(tmpStartPool)
unlabeledPool.insert(tmpUnlabeledPool)


############################
# Create a labeledPool and an unlabeledPool file to which we write the data of the labeled pool collection
# Make this text file into a conll file using makeConll. 
# # This will later be used to train epic.
tmpStartPool = startPool.find({"random" : { "$gt": 0, "$lt": 1}})
f = open(os.path.expanduser("~/epic/epic/data/labeledPool.txt"),'w')
#f = open("labeledPool.txt",'w')
#stringarray = []
for i in range(1,tmpStartPool.count()):
	if i% 100 == 0:
		print i
	f.write(str(tmpStartPool[i]))
	#	stringarray.append(tmpStartPool[i])
	f.write("\n")

f.close()
tmpUnlabeledPool = unlabeledPool.find({"random" : { "$gt": 0, "$lt": 1}})
f = open(os.path.expanduser("~/epic/epic/data/unlabeledPool.txt"),'w')
#f = open("unlabeledPool.txt",'w')
for i in range(1,tmpUnlabeledPool.count()):
	f.write(str(tmpUnlabeledPool[i]))
	f.write("\n")
	if i% 100 == 0:
		print i

f.close()



tmp_file = open(os.path.expanduser('~/epic/epic/data/labeledPool.conll'))
tmp_file.close()

noise = 0.0
if len(sys.argv) > 3:
	noise = float(sys.argv[3])
makeConll('~/epic/epic/data/labeledPool.txt', '~/epic/epic/data/labeledPool.conll',noise)

print "poop"

