#!/bin/bash
# Probably a nicer way of doing this
# @ me
git co gh-pages
git reset --hard master
ENV=production lein do clean, cljsbuild once min
rm -rf dev src target figwheel_server.log LICENSE project.clj README.md deploy.sh
cp -rv resources/public/* .
rm -rf resources
git add .
git ci -m "new deploy $(date)"
git push -f origin gh-pages
git co master
