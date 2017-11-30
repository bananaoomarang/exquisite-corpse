#!/bin/bash
ENV=production lein do clean, cljsbuild once min
rsync -avz ./resources/public milo@wordsports.xyz:/var/www/exquisite-corpse
