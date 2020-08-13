#!/bin/bash -xe

function die() {
    echo "Error: $1" >&2
    exit 1
}
[ $# -lt 2 ] && echo "Usage `basename $0` <env> <package-name> [version]" && exit 1

LOCAL_DIR=.
ENV=$1
PACKAGE=$2
VERSION=`date +%s`
[ ! -z $3 ] && VERSION=$3

[ ! -d "$LOCAL_DIR" ] && die "$LOCAL_DIR does not exist"

DEB_DIR="$LOCAL_DIR/deb"

#Copy confing file(s) -- Make this generic if we have multiple deployables in this codebase

cp ${LOCAL_DIR}/config/server_config/${ENV}/usl.yaml ${DEB_DIR}/etc/$PACKAGE/


# build app
DEF_MAVEN_CMD="<your-mvn-path>"
if [ ! -e $DEF_MAVEN_CMD ] ; then
    DEF_MAVEN_CMD=mvn
fi

MVN_CMD=${MVN_CMD:-$DEF_MAVEN_CMD}
MVN_OPTIONS="--errors -DincludeScope=runtime -DskipTests"
MVN="$MVN_CMD -U $MVN_OPTIONS"
JAVA_PATH="/usr/lib/jvm/j2sdk1.8-oracle/"
#Go to the dir and build package.
(cd ${LOCAL_DIR} && JAVA_HOME=${JAVA_PATH} ${MVN} clean install )
(cd ${LOCAL_DIR} && ${MVN} dependency:copy-dependencies )

#Copy dependencies & bootable jar
[ ! -d "$DEB_DIR/usr/lib/$PACKAGE" ] && mkdir -p "$DEB_DIR/usr/lib/$PACKAGE"
mv ${LOCAL_DIR}/container/target/container-*.jar  ${DEB_DIR}/usr/lib/$PACKAGE/fk-gap-usl.jar

#Final Deb packaging

FILES="${DEB_DIR}/etc/init.d/$PACKAGE ${DEB_DIR}/DEBIAN/*"
sed -i -e "s/_PACKAGE_/$PACKAGE/g" $FILES 
sed -i -e "s/_ENV_/$ENV/g" $FILES 
sed -i -e "s/_USER_ID_/$USER_ID/g" $FILES
sed -i -e "s/_GID_/$GID/g" $FILES
sed -i -e "s/_GROUP_/$GROUP/g" $FILES

[ ! -d "$DEB_DIR/var/log/$PACKAGE" ] && mkdir -p "$DEB_DIR/var/log/$PACKAGE"
[ ! -d "$DEB_DIR/var/run/$PACKAGE" ] && mkdir -p "$DEB_DIR/var/run/$PACKAGE"

function set_version() {
  curr_version=$1
  sed -i -e "s/_VERSION_/${curr_version}/g" ./deb/DEBIAN/control
}
function create_debian() {
  PACKAGE_NAME=$1
  echo "changing deb permissions"
  chmod -R 775 deb
  dpkg-deb -b deb ${PACKAGE_NAME}.deb
}

set_version $VERSION
create_debian $PACKAGE
