import sys
import os

numberOfSentences = int(sys.argv[1])
numberOfFiles = int(sys.argv[2])

pathToEpic = os.getcwd()
pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]


parent = open(os.path.expanduser(pathToEpic+"/data/PoolData/labeledPool.conll"),'r')
children = []
for i in range(1,numberOfFiles+1):
	filename = pathToEpic+"/data/child_conlls/childLabeledPool"+str(i)+".conll"
	children.append(filename)

parentLines = parent.readlines()

childSentences = int(numberOfSentences/numberOfFiles)
malwareLines = ""

malNumber = 0
startLine = 0

tmpSentWithMal = open(os.path.expanduser(pathToEpic+"/data/PoolData/tmp.conll"),'w')
addToFile = False
tmpSent =0
j = startLine
malNumber = 1
while(malNumber<=numberOfFiles):
	malwareLines += parentLines[j]
	if("I_MALWARE" in parentLines[j]):
		addToFile = True

	if(parentLines[j]=="\n" and addToFile == True):
		tmpSentWithMal.write(malwareLines)
		malwareLines = ""
		malNumber +=1
		addToFile = False
	elif(parentLines[j]=="\n" and addToFile == False):
		malwareLines = ""
	j+= 1

tmpSentWithMal.close()
childNumber = 0
startLine = 0
tmpSentWithMal = open(os.path.expanduser(pathToEpic+"/data/PoolData/tmp.conll"),'r')
malwareLines = tmpSentWithMal.readlines()

malwareStart = 0
addMal = True
allFiles =[]

for i in range(0,numberOfFiles):
	allFiles.append(open(os.path.expanduser(children[i]),'w'))

counter = 0
for i in range(0,len(parentLines)):
	allFiles[counter].write(parentLines[i])
	if(parentLines[i]=="\n"):
		counter += 1
	if counter > len(allFiles)-1:
		counter = 0

for i in range(0,len(allFiles)):
	while(addMal == True):
		while(malwareLines[malwareStart] != "\n"):
			allFiles[i].write(malwareLines[malwareStart])
			malwareStart+=1
		else:
			malwareStart+=1
			addMal=False
	addMal = True


tmpSentWithMal.close()
os.remove(os.path.expanduser(pathToEpic+"/data/PoolData/tmp.conll"))
parent.close()















