import os
import sys
import unicodedata
from pymongo import MongoClient
import string
import datetime
import random

######
# Add sentences to database
client = MongoClient('mon-entity-event-r13-2.recfut.com:27016')
db = client.rf_entity_curation
dataType = "negatives"
collName = "malware_epic_test_"+dataType
collection = db[collName]

f = open(os.path.expanduser("~/epic/epic/data/"+dataType+".txt"),'r')
found = False


for line in f:
	isTweet = line[line.find("u'isTweet': ")+len("u'isTweet': "):line.find(", u'",len("u'isTweet': "))]
	malware = line[line.find("u'malware': ")+len("u'malware': "):line.find("], u'",line.find("u'malware': "))]+"]"
	uniqMalware= line[line.find("u'uniqMalware': ")+len("u'uniqMalware': "):line.find("], u'",line.find("u'uniqMalware': "))]+"]"
	docID= line[line.find("u'docID': u'")+len("u'docID': u'"):line.find("', u'",line.find("u'docID': u'"))]
	numMalwareMentions= line[line.find("u'numMalwareMentions': ")+len("u'numMalwareMentions': "):line.find(", u'",line.find("u'numMalwareMentions': "))]
	numUniqueMalware= line[line.find("u'numUniqueMalware': ")+len("u'numUniqueMalware': "):line.find(", u'",line.find("u'numUniqueMalware': "))]
	sentence= line[line.find("u'sentence': u'")+len("u'sentence': u'"):line.find("', u'",line.find("u'sentence': u'"))]
	random= line[line.find("u'random': ")+len("u'random': "):line.find(", u'",line.find("u'random': "))]
	conll = line[line.find("u'conll': u'")+len("u'conll': u'"):line.find("', u'",line.find("u'conll': "))]
	sentence_type = dataType
	Title = line[line.find("u'title': u'")+len("u'conll': u'"):line.find("', u'",line.find("u'title': "))]
	_id = line[line.find("u'_id': u'")+len("u'_id': u'"):line.find("'",line.find("u'_id': u'")+len("u'_id': u'"))]
	conll = conll.replace("\\n","\n")

	print "isTweet: " + str(isTweet)
	print "malware: " + str(malware)
	print "uniqMalware: " + str(uniqMalware)
	print "docID: " + str(docID)
	print "numMalwareMentions: " + str(numMalwareMentions)
	print "numUniqueMalware: " + str(numUniqueMalware)
	print "sentence: " + str(sentence)
	print "random: "+ str(random)
	print "conll: "+ str(conll)
	print "sentence_type: "+ str(sentence_type)
	print "Title: "+ str(Title)
	print "\n\n\n"
	# Put conll, sentence and other values in database
	collection.insert( { "conll": conll, 
		"created": unicode(datetime.datetime.utcnow()),
		 "isTweet" : isTweet,
		 "malware" : malware,
		 "uniqMalware": uniqMalware,
		 "docID": docID,
		 "numMalwareMentions" : numMalwareMentions,
		 "numUniqueMalware" : numUniqueMalware,
		 "sentence": sentence,
		 "random": random,
		 "sentence_type": sentence_type,
		 "Title": Title,
		 "_id": _id
		 } )









