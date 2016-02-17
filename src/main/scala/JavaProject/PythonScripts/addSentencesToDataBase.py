import os
import sys
import unicodedata
from pymongo import MongoClient
import string
import datetime
import random

#Reads file in format, writes each sentence as a line in a new file
# Note: Each text segment in the input is enveloped by "....",
data = open(os.path.expanduser("~/epic/epic/data/"+ sys.argv[1]+".txt"),'r')
punc= ("\\/'%&-_(){}[]#0123456789\"$")
text = open(os.path.expanduser("~/epic/epic/data/"+ sys.argv[1]+".txt"+"Processed.txt"),'w')

for line in data:
    line = line[1:len(line)-3]
    replace_punctuation = string.maketrans(punc, ' '*len(punc))
    strp = line.translate(replace_punctuation)
    strp = ' '.join(strp.split())
    strp = strp.strip()
    strp = strp.replace(".",".\n")
    strp = strp.replace("!","!\n")
    strp = strp.replace("?","?\n")
    print strp
    if len(strp)>10:
        text.write(strp)


data.close()
text.close()

######
# Add sentences to database
client = MongoClient('mon-entity-event-r13-2.recfut.com:27016')
db = client.rf_entity_curation
str = "malware_"+sys.argv[1]
print str
collection = db[str]
collection.remove({})

f = open(os.path.expanduser("~/epic/epic/data/"+ sys.argv[1]+".txt"+"Processed.txt"),'r')
data_file = open(os.path.expanduser("~/epic/epic/data/"+ sys.argv[1]+"Conll.conll"),'w')
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