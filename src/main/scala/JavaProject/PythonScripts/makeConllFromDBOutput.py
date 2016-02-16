 ############
#This finds only conll sentences (from database output)
import os
import unicodedata

def strip_accents(input_str):
    nfkd_form = unicodedata.normalize('NFKD', input_str)
    only_ascii = nfkd_form.encode('ASCII', 'ignore')
    return only_ascii

def makeConll(readFile, writeFile):
	f = open(os.path.expanduser(readFile),'r')

	tmp_file = open(os.path.expanduser("~/epic/epic/data/temp.txt"),'w')
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
	tmp_file = open(os.path.expanduser("~/epic/epic/data/temp.txt"),'r')

	for line in tmp_file:
		if not "MALWARE" in line:
			if len(line)>1:
				line = line[0:len(line)-1] + "O\n"
		elif (line[len(line)-len("_MALWARE "):len(line)-1] == "_MALWARE"):
			line  = line[0:len(line)-len("_MALWARE ")]+"-MALWARE\n"
		else:
			line = line[0:len(line)-1] + "O\n"
		if (line != " . . O\n"):
			if "\\x" not in repr(line):
				if "\\u" not in repr(line):
					unicode_string = line.decode('utf-8')
					line = strip_accents(unicode_string)
					data_file.write(line.strip().decode('utf-8','ignore').encode("utf-8")+"\n")

	# for line in tmp_file:
	# 	if not "MALWARE" in line:
	# 		if len(line)>1:
	# 			line = line[0:len(line)-1] + "0\n"
	# 			if addLine:
	# 				data_file.write(prevLine)
	# 				addLine = False
	# 			data_file.write(line)
	# 		else:
	# 			data_file.write(line)

	# 	else:
	# 		if "B_MALWARE" in line:
	# 			prevLine = line
	# 			addLine = True
	# 		else:
	# 			line = line[0:len(line)-len("I_MALWARE")-1]+"B_MALWARE"
	# 			line = prevLine[0:len(prevLine)-len(". . B_MALWARE")-1]+line+ "\n"
	# 			data_file.write(line)
	# 			prevLine = ""
	# 			addLine = False




	tmp_file.close()
	os.remove(os.path.expanduser("~/epic/epic/data/temp.txt"))
	data_file.close()

	f.close()