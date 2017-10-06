#!/bin/bash

API_KEY=$1
ARCHIVE=$2
VERSION=$3

curl -T $ARCHIVE -ugayanper:$API_KEY https://api.bintray.com/content/gayanper/p2/eclipse-plugins-extras/$VERSION/eclipse-plugins-extras/$VERSION/archive-site.zip?explode=1

