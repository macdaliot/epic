import os
from getJustSentences import getJustSentences
pathToEpic = os.getcwd()
pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]
write_file = open(os.path.expanduser(pathToEpic + "/data/temp.txt"),'w')
read_file = open(os.path.expanduser(pathToEpic + "/data/Statistics/chosenIdsGibbsShort.txt"),'r')
unlab = open(os.path.expanduser(pathToEpic + "/data/PoolData/unlabeledPoolStart.txt"),'r')
lines = unlab.readlines()

for allIDs in read_file:
	ids = allIDs.split(", ")
	print len(ids)
	for id in ids:
		for line in lines:
			if id in line:
				write_file.write(line)
				print "ID: " + id
				print "Data: " + line
write_file.close()
read_file.close()
unlab.close()

writeFile = pathToEpic + "/data/Statistics/lastSentence.txt"
readFile = pathToEpic + "/data/temp.txt"
getJustSentences(readFile,writeFile)