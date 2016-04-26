import os
import unicodedata
from makeConllFromDBOutput import makeConll

pathToEpic = os.getcwd()
pathToEpic = pathToEpic[0:pathToEpic.rfind("epic")+4]

positiveFile = pathToEpic + "data/epicEvalutationTestSet/positives.txt"
positiveConll = pathToEpic + "data/epicEvalutationTestSet/positives.conll"
negativeFile = pathToEpic + "data/epicEvalutationTestSet/negatives.txt"
negativeConll = pathToEpic + "data/epicEvalutationTestSet/negatives.conll"
positiveFakeFile = pathToEpic + "data/epicEvalutationTestSet/fakePositives.txt"
positiveFakeConll = pathToEpic + "data/epicEvalutationTestSet/fakePositives.conll"

makeConll(positiveFile, positiveConll,0.0)
makeConll(negativeFile, negativeConll,0.0)
makeConll(positiveFakeFile, positiveFakeConll,0.0)

filenames = [positiveConll, positiveFakeConll, negativeConll]
with open(os.path.expanduser(pathToEpic + 'data/epicEvalutationTestSet.conll'), 'w') as outfile:
    for fname in filenames:
        with open(os.path.expanduser(fname)) as infile:
            outfile.write(infile.read())