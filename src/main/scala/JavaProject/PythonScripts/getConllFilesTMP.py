import os
import unicodedata
from makeConllFromDBOutput import makeConll

positiveFile = "~/epic/epic/data/positives.txt"
positiveConll = "~/epic/epic/data/positives.conll"
negativeFile = "~/epic/epic/data/negatives.txt"
negativeConll = "~/epic/epic/data/negatives.conll"
positiveFakeFile = "~/epic/epic/data/fakePositives.txt"
positiveFakeConll = "~/epic/epic/data/fakePositives.conll"

makeConll(positiveFile, positiveConll,0.0)
makeConll(negativeFile, negativeConll,0.0)
makeConll(positiveFakeFile, positiveFakeConll,0.0)

filenames = [positiveConll, positiveFakeConll, negativeConll]
with open(os.path.expanduser('~/epic/epic/data/epicEvalutationTestSet.conll'), 'w') as outfile:
    for fname in filenames:
        with open(os.path.expanduser(fname)) as infile:
            outfile.write(infile.read())