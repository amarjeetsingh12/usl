#! /bin/bash -e
function die()
{
        echo "Error: $1" >&2
        exit 1
}
[ -z "$LOCAL_DIR" ] && die "No LOCAL_DIR dir specified"
[ -z "$TARGET" ] && die "No package target specified"
[ -z "$PACKAGE" ] && die "No package name specified"
[ ! -d "$LOCAL_DIR" ] && die "$LOCAL_DIR does not exist"
case "$TARGET" in
        local) ENV=local;;
        nm) ENV=nm;;
        qa) ENV=eng;;
        sb) ENV=sb;;
        release) ENV=prod;;
        stagech) ENV=stagech;;
        stage) ENV=stage;;
esac

[ -z "$ENV" ] && die "Invalid target: $TARGET"

PACKAGE=usl-container
DEB_DIR="${LOCAL_DIR}"/deb


JAVAHOME='/usr/lib/jvm/j2sdk1.8-oracle'
echo "Setting Java Home to $JAVAHOME"
export JAVA_HOME=$JAVAHOME


[ ! -d "$DEB_DIR" ] && mkdir "$DEB_DIR"

mkdir -p "${DEB_DIR}"/usr/share/${PACKAGE}/
echo "BUILDING THE ENTIRE PROJECT"
mvn clean install -U

echo "CREATING JAR"
TIMESTAMP=$(date +%s)

mkdir -p "${DEB_DIR}"/usr/lib/${PACKAGE}
mkdir -p "${DEB_DIR}"/etc/usl-container/
mkdir -p "${DEB_DIR}"/etc/usl/

cp target/container-*.jar "${DEB_DIR}"/usr/lib/${PACKAGE}/${PACKAGE}.jar

sed -i "s/_PACKAGE_/"${PACKAGE}"/g" "${DEB_DIR}"/DEBIAN/*
sed -i "s/_PACKAGE_/"${PACKAGE}"/g" "${DEB_DIR}"/etc/init.d/${PACKAGE}
sed -i "s/_TARGET_/"${TARGET}"/g" "${DEB_DIR}"/etc/init.d/${PACKAGE} "${DEB_DIR}"/usr/share/${PACKAGE}/*
sed -i "s/_VERSION_/$TIMESTAMP/g" "${DEB_DIR}"/DEBIAN/*

dpkg-deb -b deb ${PACKAGE}.deb
