import os
import unicodedata
from makeConllFromDBOutput import makeConll

positiveFile = "~/epic/epic/data/epicEvalutationTestSet/positives.txt"
positiveConll = "~/epic/epic/data/epicEvalutationTestSet/positives.conll"
negativeFile = "~/epic/epic/data/epicEvalutationTestSet/negatives.txt"
negativeConll = "~/epic/epic/data/epicEvalutationTestSet/negatives.conll"
positiveFakeFile = "~/epic/epic/data/epicEvalutationTestSet/fakePositives.txt"
positiveFakeConll = "~/epic/epic/data/epicEvalutationTestSet/fakePositives.conll"

makeConll(positiveFile, positiveConll,0.0)
makeConll(negativeFile, negativeConll,0.0)
makeConll(positiveFakeFile, positiveFakeConll,0.0)

filenames = [positiveConll, positiveFakeConll, negativeConll]
with open(os.path.expanduser('~/epic/epic/data/epicEvalutationTestSet.conll'), 'w') as outfile:
    for fname in filenames:
        with open(os.path.expanduser(fname)) as infile:
            outfile.write(infile.read())