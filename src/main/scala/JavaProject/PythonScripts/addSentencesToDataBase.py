import os
import sys
import unicodedata
from pymongo import MongoClient
import string
import datetime
import random

client = MongoClient('mon-entity-event-r13-2.recfut.com:27016')
db = client.rf_entity_curation
str = "malware_"+sys.argv[1]
print str
collection = db[str]
collection.remove({})

f = open(sys.argv[1]+"Processed.txt",'r')
data_file = open(sys.argv[1]+"Conll.conll",'w')
found = False


for line in f:
	conll = ""
	words = line.split()
	for word in words:
		word = word.translate(string.maketrans("",""), string.punctuation)
		word = word + " . . \n"			
		data_file.write(word)
		conll = conll + word
	data_file.write(". . . \n")	
	data_file.write("\n")
	conll = conll + ". . . \n"
	# Put conll, sentence and other values in database
	collection.insert( { "conll": conll, 
		"created": unicode(datetime.datetime.utcnow()),
		 "isTweet" : False,
		 "malware" : [],
		 "uniqMalware": [],
		 "docID": "",
		 "numMalwareMentions" : 0,
		 "numUniqueMalware" : 0,
		 "sentence": line,
		 "random": random.random(),
		 "sentence_type": sys.argv[1],
		 "Title": ""
		 } )

data_file.close()
f.close()