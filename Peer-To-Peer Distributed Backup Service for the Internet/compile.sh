#!/bin/bash

rm -rf bin
mkdir bin
javac $(find ./src/* | grep .java) -d bin