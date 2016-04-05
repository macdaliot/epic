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

childNumber = 0
startLine = 0
for i in range(0,numberOfFiles):
	tmpFile = open(os.path.expanduser(children[childNumber]),'w')
	tmpSent =0
	j = startLine
	while(tmpSent<childSentences):
		tmpFile.write(parentLines[j])
		if(parentLines[j]=="\n"):
			tmpSent += 1
		j+= 1
	tmpFile.close()
	startLine = j
	childNumber += 1


parent.close()



















