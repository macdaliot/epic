import os

pathToEpic = os.getcwd()
pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]
read_file = open(os.path.expanduser(pathToEpic + "/data/Statistics/lastSentence.txt"),'r')
nLines =  0.0
nWords = 0.0
avWords = 0.0
for line in read_file:
	nLines +=1
	nWords += len(line.split(" "))
	avWords += sum(len(word) for word in line.split(" "))/len(line.split(" "))
print str(nWords/nLines) + " " + str(avWords/nLines)