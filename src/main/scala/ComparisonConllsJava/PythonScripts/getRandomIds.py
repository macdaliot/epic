#Get only the sentences of the database (from a txt file)
import os

pathToEpic = os.getcwd()
pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]

ids = open(os.path.expanduser(pathToEpic+"/data/PoolData/labeledPoolStartIds.txt"),'r')
read_file = open(os.path.expanduser(pathToEpic+"/data/allSentences.txt"),'r')
write_file = open(os.path.expanduser(pathToEpic+"/data/allSentencesNoLabeled.txt"),'w')

allIds = ids.readlines()

for line in read_file:
	add = True
	tmp = line.split(" ")
	for oneId in allIds:
		test = str(oneId)
		if str(tmp[0])==test[0:len(test)-1]:
			add = False

	if add:
		write_file.write(line[0:len(line)-1]+"\n".decode('string_escape'))

write_file.close()
read_file.close()