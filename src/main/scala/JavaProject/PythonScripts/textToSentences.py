#Reads file in format, writes each sentence as a line in a new file
# Note: Each text segment in the input is enveloped by "....",
import pymongo
import sys
import os
from pymongo import MongoClient
import urllib, json
import string

data = open(~epic/epic/data/sys.argv[1]+".txt",'r')#json.loads(response.read())
punc= ("\\/'%&-_(){}[]#0123456789\"$")
text = open(~epic/epic/data/sys.argv[1]+"Processed.txt",'w')

for line in data:
	line = line[1:len(line)-3]
	replace_punctuation = string.maketrans(punc, ' '*len(punc))
	strp = line.translate(replace_punctuation)
	strp = ' '.join(strp.split())
	strp = strp.strip()
	#strp = strp.replace(". ",".\n")
	#strp = strp.replace("! ","!\n")
	#strp = strp.replace("? ","?\n")
	strp = strp.replace(".",".\n")
	strp = strp.replace("!","!\n")
	strp = strp.replace("?","?\n")
	#Men Ta inte bort de. 
	print strp
	if len(strp)>10:
		text.write(strp)


data.close()
text.close()










