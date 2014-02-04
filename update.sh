set -e

APP_FOLDER_NAME=adr-appLiveNation
TARGET_GIT_BRANCH=master

update_or_install() {
	cd "${CODE_DIR}"
	
	if [ ! -d ./$1 ]; then
		echo "****"
		echo "Getting $1 from github"
		echo
		git clone $2
	else
		echo "****"
		echo "Updating $1"
		echo
		cd $1 && git pull
		cd ..
	fi
}

cwd=${PWD##*/}
if [ "${cwd}" != "${APP_FOLDER_NAME}"  ]; then
  echo "****"
  echo "You are not in the right directory. You should be in '${APP_FOLDER_NAME}', not '${cwd}'"
  exit
fi

GIT_BRANCH=$(git branch | sed -n -e 's/^\* \(.*\)/\1/p')

if [ ${GIT_BRANCH} != "${TARGET_GIT_BRANCH}" ]; then
  echo "You are not on the '${TARGET_GIT_BRANCH}' branch. Aborting. Branch is ${GIT_BRANCH}"
  exit
fi

echo "****"
echo "Updating ${APP_FOLDER_NAME} project and associated projects"
echo

cd ..
CODE_DIR=`pwd`

update_or_install "${APP_FOLDER_NAME}" git@github.com:TeamSidewinder/adr-appLiveNation.gi
update_or_install adr-libsLabsPlatform git://github.com/TeamSidewinder/adr-libLabsPlatform.git
update_or_install adr-libsThirdParty git@github.com:TeamSidewinder/adr-libsThirdParty.git
