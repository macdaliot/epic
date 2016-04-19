#Get only the sentences of the database (from a txt file)
import os

def getJustSentences(readfile, writefile):
	test_file = open(os.path.expanduser(readfile),'r')
	write_file = open(os.path.expanduser(writefile),'a')

	for line in test_file:
		tmp = line
		start = tmp.find("sentence': u") + 13
		end = tmp.find(", u'",start)
		tmp = tmp[start:end-1]+"\n"
		write_file.write(tmp.decode('string_escape'))

	write_file.close()
	test_file.close()