 ############
#This finds only conll sentences (from database output)
import os
import unicodedata
import random

def strip_accents(input_str):
    nfkd_form = unicodedata.normalize('NFKD', input_str)
    only_ascii = nfkd_form.encode('ASCII', 'ignore')
    return only_ascii

def makeConll(readFile, writeFile):
	pathToEpic = os.getcwd()
	pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]
	f = open(os.path.expanduser(readFile),'r')

	tmp_file = open(os.path.expanduser(pathToEpic + "/data/temp.txt"),'w')
	data_file = open(os.path.expanduser(writeFile),'w')
	found = False

	for line in f:
		tmp = line
		start = tmp.find("conll': u'") + 10
		end = tmp.find("', u'malware",start)
		tmp = tmp[start:end]#+"\n"
		tmp = tmp + "\n"
		tmp_file.write(tmp.decode('string_escape'))

	tmp_file.close()
	#tmp_file = open(os.path.expanduser(pathToEpic + "/data/temp.txt"),'r')
	#malwareRemoved = False


	#for line in tmp_file:
	#	if not "_MALWARE" in line:
	#		if len(line)>1:
	#			if (rand > noise):
	#				line = line[0:len(line)-1] + "O\n"
	#			else:
	#				line = line[0:len(line)-1] + "B_MALWARE\n"
	#	elif "_MALWARE" in line:
	#		if malwareRemoved:
	#			line  = line[0:len(line)-len("I_MALWARE ")]+"O\n"
	#			malwareRemoved = False
	#		elif(rand < noise):
	#			line  = line[0:len(line)-len("B_MALWARE ")]+"O\n"
	#			malwareRemoved = True
	#	if (line != " . . O\n"):
	#		if "\\x" not in repr(line):
	#			if "\\u" not in repr(line):
	#				unicode_string = line.decode('utf-8')
	#				line = strip_accents(unicode_string)
	#				data_file.write(line.strip().decode('utf-8','ignore').encode("utf-8")+"\n")


	#tmp_file.close()
	os.remove(os.path.expanduser(pathToEpic + "/data/temp.txt"))
	data_file.close()

	f.close()