#!/bin/bash

cd karaf/target
rm -rf bwreserv-karaf-0.1.0-SNAPSHOT
unzip bwreserv*.zip
cd bwreserv*
./bin/karaf
