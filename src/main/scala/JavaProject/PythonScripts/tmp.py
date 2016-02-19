import pymongo
import sys
import os
from pymongo import MongoClient
from makeConllFromDBOutput import makeConll


makeConll('~/epic/epic/data/unlabeledPool.txt', '~/epic/epic/data/unlabeledPool.conll')

print "poop"

