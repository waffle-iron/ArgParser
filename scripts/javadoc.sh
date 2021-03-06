#!/usr/bin/env bash
set -v

if [ "$TRAVIS_REPO_SLUG" == "mibac138/ArgParser" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_TAG" == "" ]; then

  echo -e "Generating Javadoc\n"

  ./gradlew dokka
  
  echo -e "Generated Javadoc\n"
  echo -e "Publishing Javadoc\n"

  cp -R "build/javadoc/" $HOME/javadoc-latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/mibac138/ArgParser gh-pages > /dev/null


  cd gh-pages
  git rm -rf ./docs/nightly
  mkdir -p "./docs/nightly" && cp -Rf $HOME/javadoc-latest/. "$_"


  git add -f .
  git commit -m ":sparkles: Auto pushed commit $TRAVIS_COMMIT"
  git push -fq origin gh-pages > /dev/null
  echo -e "Published Javadoc to gh-pages.\n"
fi
