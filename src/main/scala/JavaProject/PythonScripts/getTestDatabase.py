import pymongo
import sys
import os
from pymongo import MongoClient
from makeConllFromDBOutput import makeConll
#from getJustSentences import getJustSentences



client = MongoClient('mon-entity-event-r13-2.recfut.com:27016')
db = client.rf_entity_curation
allMalware = db.new_malware


positives = allMalware.find({"sentence_type" : "DanielMeningar"})
negatives = allMalware.find({"$and": [ { "malware":{"$size":0}},  {"random" : { "$gt": 0, "$lt": 1 }} ]})
nOfPositives = 0
nOfFakePositives = 0
nOfNegatives = 0
i = 1
malwares = ""
#positiveFile = open(os.path.expanduser("~/epic/epic/data/positives.txt"),'w')
#positiveFakeFile = open(os.path.expanduser("~/epic/epic/data/fakePositives.txt"),'w')
while (nOfFakePositives < 100):
    line = str(positives[i])
    malware = positives[i]['malware']
    add =  True
    if (nOfPositives<=400):
        for mal in malware:
            if (malwares.count(str(mal))>=3):
                add = False

        if (add):
            positiveFile.write(str(positives[i])+ "\n")
            nOfPositives+=1
            print nOfPositives, i
            malwares += str(malware) + " "

    else:
        positiveFakeFile.write(str(positives[i])+ "\n")
        nOfFakePositives+=1
    i+=1

negativeFile = open(os.path.expanduser("~/epic/epic/data/negatives.txt"),'w')

i=1
while (nOfNegatives < 500):
			negativeFile.write(str(negatives[i])+ "\n")
			nOfNegatives+=1 
			i+=1
			print i

negativeFile.close()
#positiveFile.close()
#positiveFakeFile.close()


