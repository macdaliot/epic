from collections import Counter
import os
import string

pathToEpic = os.path.abspath(os.path.join(os.path.dirname(os.getcwd()),"../../../.."))

f = open(os.path.expanduser(pathToEpic+"/data/conllFileTraining.conll"),'r')

fileFreq = open('wordFreq.txt','w')

#punc= string.punctuation + string.digits #(",./;'?!%&-_:(){}[]#0123456789\"$")


tmp = open('tmp.txt','w')
for line in f:
	if (len(line) > 4):
		if "MALWARE" in line:
			line = line[0:len(line)-len(" . . B-MALWARE ")]+"\n"
		else: 
			line = line[0:len(line)-len(" . . ")]+"\n"

	#replace_punctuation = string.maketrans(punc, ' '*len(punc))
	
		strp = line.lower()#.translate(replace_punctuation)
		tmp.write(strp.decode('string_escape'))


tmp.close()
tmp = open('tmp.txt','r')
f.close()

wordcount = Counter(tmp.read().split())
totalWords = sum(wordcount.values()) 
print totalWords
max = 0;


for item in wordcount.items(): 
	fff = float("{} {}".format(*item).rpartition(' ')[2])/totalWords
	if (fff*totalWords>max):
		max = fff*totalWords
		word = "{} ".format(*item)
	fileFreq.write("{} ".format(*item)+ str(fff) +"\n")
print str(max) + " :" +word
os.remove('tmp.txt')







#fileList = open('wordList.txt','w')

# fSplit = f.read().split()
# unique_words = set(fSplit) 
# for word in unique_words:
# 	temp = str(word) + "\n"
# 	fileList.write(temp.decode('string_escape'))

# fileList.close()
# fileList = open('wordList.txt','r')
# counter = 0
# for line in fileList:
# 	word = line.decode('string_escape')
# 	nWords = str(f.read().split()).count(word)

# 	counter += 1
# 	fileFreq.write(word + " " + str(nWords) +"\n")


